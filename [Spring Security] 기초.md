# [Spring Security] 기초

>  스프링 부트에서 진행 함
>
> 인텔리제이에서 Ctrl+v

## 연동 및 설정

* 의존성 추가

  * 스타터 사용

  * 버전 생략 - 스프링 부트의 의존성 관리 기능 사용

    ```
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

