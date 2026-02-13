# Capstone_25_1 
## AI 기반 실시간 인구 밀집 모니터링 및 예측 앱(도시온도)




**1. 프로젝트명**

- AI 기반 실시간 인구 밀집 모니터링 및 예측 앱 개발




**2. 프로젝트 인원**
- 컴퓨터공학과: 김가람, 김진경, 안소연, 조하연 4인




**3. 작품 설명**

**<시스템 구성도>**

<img width="436" height="317" alt="image" src="https://github.com/user-attachments/assets/b6d6a035-06c7-4865-8490-8a5894157e18" />


∙ 백엔드 서버: Spring Boot (Java)

∙ 프론트엔드(앱) : Android (Java/Groovy)

∙ 데이터베이스(DB): AWS RDS (MySQL)

∙ 실시간 혼잡도: Python, SKT Tmap API

∙ AI 예측 모델: Python, OpenAI API 활용

∙ 지도 API: Google Maps API

∙ 서버 운영: AWS EC2



**<데이터 베이스>**

<img width="427" height="336" alt="image" src="https://github.com/user-attachments/assets/a1e6870a-ac6d-4910-adc2-b2acd7da0018" />




**<개발 세부 내용>**

1. 데이터 수집 및 저장

∙ 각 장소에 대해 Google Maps를 활용해 위도/경도 좌표, 주소를 추출하고 이를 JSON 형식으로 저장 후 DB(Locations 테이블)에 등록 및 활용 가능한 형태로 구축

∙ SKT Tmap API를 이용하여 JSON에 저장된 위도/경도를 기반으로 도로 교통 속도 및 혼잡도 데이터를 매 시간마다 수집 (cron 이용하여 1시간 주기로 수행되도록 구성)

∙ 혼잡도는 차량속도에 따라 4단계로 나뉘어 저장됨

∙ 수집된 데이터는 DB(PopulationFlow 테이블)에 저장 

<img width="402" height="54" alt="image" src="https://github.com/user-attachments/assets/a76fb9ed-d9aa-4fbf-8c1e-b700414252c6" />

(Locations 테이블)

<img width="241" height="65" alt="image" src="https://github.com/user-attachments/assets/79285ca5-ee04-445e-8587-a63ae09509c2" />

(PopulationFlow 테이블)

2. AI 예측 기능
∙ DB에 저장된 기반으로 요일, 시간대별 혼잡도 학습

∙ Python으로 OpenAI API를 불러와 예측 처리

∙ 예측 결과는 DB(Prediction 테이블)에 저장

∙ 이후 데이터가 새로 수집될 때마다 이전 예측값과 재계산하여 새로운 예측값을 저장함

∙ 사용자는 원하는 장소/요일/시간을 입력해 예측 혼잡도를 조회 가능

<img width="326" height="108" alt="image" src="https://github.com/user-attachments/assets/c7f4c8ca-0c07-4943-91af-4f4e60aec076" />

(Prediction 테이블)

4. 사용자 기능

∙ 실시간 지도 시각화(메인화면) : Google Maps에 현재 혼잡도를 색상으로 표현

∙ 예측 검색: 사용자가 날짜/시간/장소 입력 -> 예측 혼잡도 표시

∙ 추천 기능: 카테고리별, 혼잡도별(붐비는 곳/한적한 곳) 장소 추천

∙ 즐겨찾기: 자주 찾는 장소를 저장하여 빠르게 접근 가능


4. 서버 - 앱 통신

∙ 앱은 Retrofit 사용하여 Spring Boot 서버와 JSON 기반 통신

∙ 서버는 /api/predict, /api/recommendation, /api/favorites 등 REST API 제공

∙ 앱은 필요한 각 기능 탭(Home, 추천, 즐겨찾기 등)에서 필요한 API를 호출하여 데이터 처리

5. DB 설계 및 연동

∙ 앱은 Retrofit 사용하여 Spring Boot 서버와 JSON 기반 통신

∙ 서버는 /api/predict, /api/recommendation, /api/favorites 등 REST API 제공

∙ 앱은 필요한 각 기능 탭(Home, 추천, 즐겨찾기 등)에서 필요한 API를 호출하여 데이터 처리

6. 운영 및 보안 설계

   
∙ 로그인 및 회원가입 시, 사용자 비밀번호는 SHA-256 해시 알고리즘으로 암호화하여 저장

∙ Spring Boot 서버는 AWS EC2 인스턴스에 배포하여 운영

∙ 서버 내에서 크론탭(crontab) 으로 스크립트 자동 실행을 통해 1시간마다 실시간 데이터 수집 작업을 자동화함. 

∙ Android 앱은 release.keystore로 서명된 .apk 파일로 빌드하여 배포하며, Google Maps API 연동을 위해 릴리즈 키의 SHA-1 인증서를 등록함.



**4. 구현 결과**

<홈 화면>

<img width="647" height="301" alt="image" src="https://github.com/user-attachments/assets/313deaa6-3b6a-4489-a645-e81c374a065c" /> 

현재 혼잡도에 따라 지도에 표시된 모습이며, 장소를 검색하면 해당 지역으로 이동 후 혼잡도를 색깔과 숫자로 확인한다.




<예측 탭>

<img width="425" height="296" alt="image" src="https://github.com/user-attachments/assets/f1107c90-0b1c-4759-9a20-46903058d6a5" /> 

원하는 장소를 입력 후 예측할 날짜와 시간을 선택하면 해당 장소의 혼잡도 예측 결과를 확인할 수 있다.

하단에는 해당 장소와 시간의 일주일 혼잡도를 표로 표시한다.



<추천탭>

<img width="290" height="291" alt="image" src="https://github.com/user-attachments/assets/0de55bfe-e056-4c28-8726-7f8f51ae5a56" /> 

카테고리(식당, 카페, 쇼핑, 자연명소 등)에 따라 장소를 확인할 수 있으며, 붐비는 순/한적한 순을 통해 보기 쉽게 정렬된다. 

각 장소 별로 지금 현재 시간의 혼잡도가 어떤지도 확인이 가능하다.



<즐겨찾기>

<img width="297" height="294" alt="image" src="https://github.com/user-attachments/assets/c67a0f43-7d1f-4d7d-aee1-283401cbadec" />

추천 탭에서 원하는 장소를 발견하여 즐겨찾기(하트)를 누르면 즐겨찾기 탭에서 따로 확인이 가능하다. 

로그인 했을 시에 사용이 가능하며, 로그인을 하지 않으면 로그인 후 기능을 이용할 수 있다는 문구가 뜬다. 



<로그인/ 회원가입>

<img width="302" height="297" alt="image" src="https://github.com/user-attachments/assets/9b615bbc-03d1-42d5-a59a-1c5d0efbc293" /> 

회원가입 시 성명, 이메일, 아이디, 비밀번호를 입력하고 로그인을 하면 마이페이지에서 로그인이 성공한 모습을 확인할 수 있다. 

**5. 본인 역할**

