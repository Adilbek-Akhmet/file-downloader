const togglePassword = document.querySelector('#togglePassword');
const password1 = document.querySelector('#password1');
const password2 = document.querySelector('#password2');


togglePassword.addEventListener('click', function (e) {
    // toggle the type attribute
    const type1 = password1.getAttribute('type') === 'password' ? 'text' : 'password';
    const type2 = password2.getAttribute('type') === 'password' ? 'text' : 'password';
    password1.setAttribute('type', type1);
    password2.setAttribute('type', type2);
});

$(document).ready(function(){
    $("#close").click(function(){
        console.log(document.querySelector("#myAlert"));
        document.querySelector("#myAlert").style.display = "none";
    });
});