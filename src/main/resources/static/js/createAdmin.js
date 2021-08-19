const togglePassword = document.querySelector('#togglePassword');
const password = document.querySelector('#password');

togglePassword.addEventListener('click', function (e) {
    // toggle the type attribute
    const type = password.getAttribute('type') === 'password' ? 'text' : 'password';
    password.setAttribute('type', type);
});

$(document).ready(function(){
    $("#close").click(function(){
        console.log(document.querySelector("#myAlert"));
        document.querySelector("#myAlert").style.display = "none";
    });
});