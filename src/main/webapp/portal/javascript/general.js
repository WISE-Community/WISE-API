function impersonateUser(username) {
  $.post( "/api/login/impersonate", {username: username}).done(function( data ) {
    window.location.href = "/";
  });
}

function switchBackToOriginalUser() {
$.post( "/api/logout/impersonate").done(function( data ) {
    window.location.href = "/teacher";
  });
}
