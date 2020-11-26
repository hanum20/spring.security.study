# [Spring Security] 기초

>  스프링 부트에서 진행 함
>
> 인텔리제이에서 Ctrl+v

## 연동 및 설정

* 의존성 추가

  * 스타터 사용

  * 버전 생략 - 스프링 부트의 의존성 관리 기능 사용

    ```xml
    <dependency>
    	<groupId>org.springframework.boot</groupId>
    	<artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    ```

  * 의존성이 추가되면 모든 요청이 인증을 필요로 하게 됨.

  * 기본 유저가 생성 됨.

    * username: user
    * Password: 콘솔에 출력 됨
    * 어떠한 role도 없는 상태

* 설정

  * Config 추가

    ```java
    @Configuration
    @EnableWebSecurity
    public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                    .mvcMatchers("/", "/info").permitAll() // 해당 요청은 모두 허용
                    .mvcMatchers("/admin").hasRole("ADMIN") //ADMIN 권한을 가져야 admin
                    .anyRequest().authenticated(); // 그외 요청들은 인증을 요청하기만 하면 됨
    
            http.formLogin(); // form 로그인을 사용할 것임
            http.httpBasic();
        }
    }
    ```

    * `authorizeRequests()`
      * 요청 인증에 대한 설정을 해당 메소드로 시작
      * Ant pattern을 사용 가능
    * `and()`로 한 번에 설정해도 되지만, 가독성을 생각하여 위처럼 설정도 가능

## 커스터마이징: 인메모리 유저

* 기본 사용자는 `UserDetailsServiceAutoConfiguration`에 설정이 되어있음

  * 해당 클래스가 참조중인 프로퍼티 클래스를 보니, 사용자 이름, 패스워드, role에 대한 getter, setter가 구현되어있음.
  * password를 임의로 정의하면. 자동생성하지 않음

* properties 파일에서도 설정 가능

  ```properties
  spring.security.user.name=admin
  spring.security.user.password=123
  spring.security.user.roles=ADMIN
  ```

  * 사용자 한 명에 대한 정보 밖에 없어서 별로 좋은건 아님. 알고만 있으면 됨

* config에서 `AuthenticationManagerBuilder`를 파라메터로 받는 `configure` 메소드를 오버라이드하여 정의 할 수 도 있음

  ```java
  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
      auth.inMemoryAuthentication()
              .withUser("oneum").password("{noop}123").roles("USER").and()
              .withUser("admin").password("{noop}!@#").roles("ADMIN");
  }
  
  ```

  * `{noop}`는 페스워드 인코딩을 하지 않겠다는 뜻



## JPA 연동

* JPA와 H2 의존성 추가

  ```xml
  <dependency>
  	<groupId>org.springframework.boot</groupId>
  	<artifactId>spring-boot-starter-data-jpa</artifactId>
  </dependency>
  <dependency>
  	<groupId>com.h2database</groupId>
  	<artifactId>h2</artifactId>
  	<scope>runtime</scope>
  </dependency>
  ```

* 유저 정보를 나타낼 클래스 추가

  ```
  @Entity
  public class Account {
  
      @Id @GeneratedValue
      private Integer id;
  
      @Column(unique = true)
      private String username;
  
      private String password;
  
      private String role;
  ...
  }
  ```

* config 변경

  ```java
  @Configuration
  @EnableWebSecurity
  public class SecurityConfig extends WebSecurityConfigurerAdapter {
  
      @Override
      protected void configure(HttpSecurity http) throws Exception {
          http
              // Request 설정
              .authorizeRequests()
                  .antMatchers("/", "/info", "/account/**").permitAll()
                  .antMatchers("/admin/**").hasRole("ADMIN")
                  .anyRequest().authenticated()
              .and()
              // 로그인 설정
              .formLogin()
              .and()
              // 요청 설정
              .httpBasic();
      }
  }
  ```

* JpaRepository

  ```java
  public interface AccountRepository extends JpaRepository<Account, Long> {
  
      Account findByUsername(String username);
  }
  ```

*  Service

  ```java
  @Service
  public class AccountService implements UserDetailsService {
  
      @Autowired
      AccountRepository accountRepository;
  
      @Override
      public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
          Account account = accountRepository.findByUsername(username);
  
          if(account == null){
              throw new UsernameNotFoundException(username);
          }
  
        
          return User.builder()
                  .username(account.getUsername())
                  .password(account.getPassword())
                  .roles(account.getRole())
                  .build();
      }
  
      public Account createAccount(Account account) throws Exception {
          account.setPassword("{noop}" + account.getPassword());
  
          return accountRepository.save(account);
      }
  }
  ```

  * 예전에는 `User`가 없어서 어댑터를 직접 구현하여 UserDetails를 리턴해 주어야 했었다.

    * 이 어댑터는 상황에 따라서 구현이 필요할 수도 있음

  * 패스워드 인코더를 위와 같이 설정한다.

    ```java
    account.setPassword("{noop}" + account.getPassword());
    ```

* Controller

  ```java
  @RestController
  public class AccountController {
  
      @Autowired
      AccountService accountService;
  
      @GetMapping("/account/{role}/{username}/{password}")
      public Account createAccount(@ModelAttribute Account account) throws Exception {
          return accountService.createAccount(account);
      }
  }
  ```

  ```java
  @RestController
  public class HelloController {
  
      @GetMapping
      public String index(){
          return "Hello Security!";
      }
  
      @GetMapping("/user/info")
      public String userInfo() {
          return "Hello User";
      }
  
      @GetMapping("/admin/info")
      public String adminInfo() {
          return "Hello admin";
      }
  }
  ```

* `UserDetailsService`

  * spring security에서 옴
  * 인증 관리 할 때, DAO 인터페이스를 이용해서 데이터베이스의 사용자 정보를 다룬다.
    * 여기서는 JPA를 사용했기 때문에 JPA 구현제를 주입받아 사용한다.
  * 디비의 종류에 제한은 없음

*  스프링 시큐리티는 특수한 패스워드 패턴을 요구함.

  * 패스워드 인코더

    ```
    123 (X)
    {noop}123 (O)
    ```

*  샘플 코드 로직

  * 제한된 페이지(`/user/info`)로 접근 > 로그인 페이지 리다이렉트 > 회원가입 요청(`/account/{role}/{username}/{password}`) > 결과 리턴 > 제한된 페이지(`/user/info`) 접근 > 로그인 페이지 > 로그인 성공 > 기존 접근하려던 제한된 페이지로 리다이렉트 > 성공

