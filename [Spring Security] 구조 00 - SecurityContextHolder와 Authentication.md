# [Spring Security] 구조 00 - SecurityContextHolder와 Authentication

> Principle(인증된 사용자 정보)를 Authentication에 저장한다.
>
> Authentication을 다시 SecurityContext에 저장한다.



Authentication

* Principal과 GrantAuthority 제공.



Principal

* “누구"에 해당하는 정보.
* **UserDetailsService에서 리턴한 그 객체.**
* 객체는 UserDetails 타입.



GrantAuthority: 

* “ROLE_USER”, “ROLE_ADMIN”등 Principal이 가지고 있는 “권한”을 나타낸다.
* 인증 이후, 인가 및 권한 확인할 때 이 정보를 참조한다.



UserDetails

* 애플리케이션이 가지고 있는 유저 정보와 스프링 시큐리티가 사용하는 Authentication 객체 사이의 어댑터.



UserDetailsService

* 유저 정보를 UserDetails 타입으로 가져오는 DAO (Data Access Object) 인터페이스.
* 구현은 마음대로



SecurityContextHolder

* SecurityContext를 제공한다.

  * 제공 방법

    * ThreadLocal

      * 한 쓰레드 내에 저장

    * 현재 접속 중인 사용자의 정보를 알고싶다.

      ```java
      public void dashboard() {
          Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
          Object principal = authentication.getPrincipal();
          Collection<? extends GrantedAuthority> authorites = authentication.getAuthority();
          Object credentials = authentication.getCredentials();
          boolean authenticated = authentication.isAuthenticated();
      }
      ```

      

  