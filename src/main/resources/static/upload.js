const togglePassword = document.querySelector('#togglePassword');
const password = document.querySelector('#password');

togglePassword.addEventListener('click', function (e) {
    // toggle the type attribute
    const type = password.getAttribute('type') === 'password' ? 'text' : 'password';
    password.setAttribute('type', type);
    // toggle the eye slash icon
    this.classList.toggle('fa-eye-slash');
});

const login = document.querySelector('#login');
const pass = document.querySelector('#password');
const btn = document.querySelector('#btn');

function randomGenerate() {
    var length = 8,
        charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789",
        retVal = "";
    for (var i = 0, n = charset.length; i < length; ++i) {
        retVal += charset.charAt(Math.floor(Math.random() * n));
    }
    return retVal;
}

btn.addEventListener('click', () => {
    login.value = randomGenerate();
    pass.value = randomGenerate();
})

$(document).ready(function(){
    $(".close").click(function(){
        $("#myAlert").alert('close');
    });
});