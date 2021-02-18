function impersonateUser(username) {
  $(`<form style="visibility:hidden" action="/api/login/impersonate" method="POST"><input name="username" value="${username}"/></form>`)
      .appendTo('body').submit();
}

function switchBackToOriginalUser() {
  $(`<form style="visibility:hidden" action="/api/logout/impersonate" method="POST"></form>`)
      .appendTo('body').submit();
}
