let host = 'http://' + window.location.host;

$(document).ready(function () {
    const auth = getToken();
    //토큰을 가져와서 로그인 상태인지 확인하는거임
    //로그인 X 상태라면 if문 들어가서 해당 url로 자동으로 페이지 이동!
    //로그인 O 상태라면 홈 페이지 보여주는거임
    if(auth === '') {
        window.location.href = host + "/api/user/login-page";
    } else {
        $('#login-true').show();
        $('#login-false').hide();
    }
})

function logout() {
    // 토큰 삭제
    Cookies.remove('Authorization', { path: '/' });
    window.location.href = host + "/api/user/login-page";
}

function getToken() {
    let auth = Cookies.get('Authorization');

    if(auth === undefined) {
        return '';
    }

    return auth;
}