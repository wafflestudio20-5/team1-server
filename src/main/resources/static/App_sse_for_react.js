import logo from './logo.svg';
import './App.css';

import { EventSourcePolyfill } from 'event-source-polyfill/src/eventsource.min.js'


// For frontend, 참고 부탁드립니다: https://velog.io/@green9930/%EC%8B%A4%EC%A0%84-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-React%EC%99%80-SSE
var accessToken;

function login() {
          accessToken = document.getElementById('accessToken').value;

          console.log("try to connect eventsource");
          console.log("token: " + accessToken);
          
          // localhost:8080 -> api.wafflytime.com으로 변경
          const eventSource = new EventSourcePolyfill(`http://localhost:8080/api/sse-connect`, {
            headers: {
              'Authorization': 'Bearer ' + accessToken
            }
          });
  
          eventSource.addEventListener("sse", function (event) {
              console.log(event.data);
  
              const data = JSON.parse(event.data);
  
              (async () => {
                  // 브라우저 알림
                  const showNotification = () => {
                          if (data.notificationType === 'REPLY') {
                            alert("notificationId: " + data.notificationId + "\n" + data.content + "\n" + "게시판 id: " + data.info.boardId + "\n" +
                                  "게시판: " + data.info.boardTitle + "\n" + "게시물 id: " + data.info.postId);
                          } else {
                            alert(data.content);
                          }
                  }
  
                  // 브라우저 알림 허용 권한
                  let granted = false;
  
                  if (Notification.permission === 'granted') {
                      granted = true;
                  } else if (Notification.permission !== 'denied') {
                      let permission = await Notification.requestPermission().then(function (permission) {
                        console.log(permission)
                         });
                      granted = permission === 'granted';
                  }
  
                  // 알림 보여주기
                  if (granted) {
                      console.log("알람 보여줘!");
                      showNotification();
                  }
              })();
          })
      }
  
      function check() {
        const notificationId = document.getElementById('notificationId').value;
        // localhost:8080 -> api.wafflytime.com으로 변경
        fetch('http://localhost:8080/api/notification-check/' + notificationId, {
          method: "PUT",
          headers: {
            Authorization : 'Bearer ' + accessToken
          }
        })
          .then((response) => response.json())
          .then((data) => console.log(data));
      }
function App() {
  return (
    <div className="App">
      <header className="App-header">
        <input type="text" id="accessToken"/>
        <button type="button" onClick={login}>로그인</button>
        <input type="text" id="notificationId"/>
        <button type="button" onClick={check}>알림확인버튼(notification id 입력)</button>

      </header>
    </div>
  );
}

export default App;
