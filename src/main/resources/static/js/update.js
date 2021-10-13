let pswd_done = false;
let pswd_input = document.getElementById("password1");
let pswd_res = document.getElementById("password1-res");

let rep_pswd_input = document.getElementById("password2");
let rep_pswd_res = document.getElementById("password2-res");
rep_pswd_input.disabled = true;

pswd_input.addEventListener("input", password_check);
rep_pswd_input.addEventListener("input", rep_password_check);

function password_check() {
  if (this.value.length >= 3) {
    pswd_res.innerHTML = "";
    rep_pswd_input.disabled = false;
  } else {
    pswd_res.innerHTML =
        "Пароль должен состоять из три или более символов";
    pswd_res.style.color = "#d13838";
    rep_pswd_input.disabled = true;
  }
  if (rep_pswd_input.value != "") {
    rep_password_check();
  }
}

function rep_password_check() {
  if (pswd_input.value === this.value) {
    pswd_done = true;
    rep_pswd_res.innerHTML = "";
  } else {
    pswd_done = false;
    rep_pswd_res.innerHTML = "Пароли не совпадают";
    rep_pswd_res.style.color = "#d13838";
  }
}

function validate() {
  console.log(pswd_done);
  if (pswd_done) {
    return true;
  } else {
    return false;
  }
}

let passwordButtons = document.querySelectorAll(".password-btn");
let results = document.querySelector(".result");
let usersId = document.querySelectorAll(".userId");
let closeEditButton = document.querySelector("#close-password-btn");
let passwordModal = document.querySelector("#password-modal");

for (let i = 0; i < passwordButtons.length; ++i) {

  passwordButtons[i].addEventListener("click", () => {
    console.log(i);
    passwordModal.style.display = "flex";
    results.value = usersId[i].value;
  });
}

closeEditButton.addEventListener("click", () => {
  pswd_input.value = "";
  rep_pswd_input.value = "";
  passwordModal.style.display = "none";
});

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
