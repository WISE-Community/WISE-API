// Attach the CSRF token to state changing AJAX requests sent from these portal pages.
// The server stores the token in a cookie that is readable by this script and expects
// it to be echoed back in a request header, which is how it confirms the request was
// made by the page and not forged by another site. Without this, jQuery POST requests
// on these pages would be rejected once CSRF protection is enabled.
(function () {
  function getCookie(name) {
    var match = document.cookie.match(new RegExp('(^|;\\s*)' + name + '=([^;]*)'));
    return match ? decodeURIComponent(match[2]) : null;
  }
  $(document).ajaxSend(function (event, jqxhr, settings) {
    var method = (settings.type || 'GET').toUpperCase();
    var isStateChanging =
      method !== 'GET' && method !== 'HEAD' && method !== 'OPTIONS' && method !== 'TRACE';
    if (!isStateChanging || settings.crossDomain) {
      return;
    }
    var token = getCookie('XSRF-TOKEN');
    if (token) {
      jqxhr.setRequestHeader('X-XSRF-TOKEN', token);
    }
  });
})();

function impersonateUser(username, userType) {
  $.post( "/api/login/impersonate", {username: username}).always(function( data ) {
    window.location.href = `/${userType}`;
  });
}

function switchBackToOriginalUser() {
$.post( "/api/logout/impersonate").always(function( data ) {
    window.location.href = "/teacher";
  });
}
