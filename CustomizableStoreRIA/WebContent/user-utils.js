(() => {
    // Handle user click on 'register' link.
    // Transform the login form into a registration form.
    document.getElementById('toggle-register').addEventListener('click', () => {
        const form = document.getElementById('home-form');

        const checkPwdInput = document.createElement('input');
        checkPwdInput.id = 'user-check-password';
        checkPwdInput.type = 'password';
        checkPwdInput.name = 'checkPassword';
        checkPwdInput.required = true;
        checkPwdInput.className = 'custom-input';
        checkPwdInput.placeholder = 'check password';

        const emailInput = document.createElement('input');
        emailInput.id = 'register-email';
        emailInput.type = 'text';
        emailInput.name = 'email';
        emailInput.placeholder = 'email';
        emailInput.className = 'custom-input';
        emailInput.autocomplete = 'off';
        emailInput.spellCheck = 'false';
        emailInput.autocorrect = 'off';
        emailInput.required = true;

        const roleOptions = [{
            value: 'ROLE_EMPLOYEE',
            label: 'Employee'
        }, {
            value: 'ROLE_CLIENT',
            label: 'Client'
        }];
        const roleSelection = document.createElement('select');
        roleSelection.id = "register-role-selection";
        roleSelection.name = "role";
        roleSelection.required = true;
        roleSelection.className = 'custom-input';
        roleOptions.forEach(r => {
            const opt = document.createElement('option');
            opt.value = r.value;
            opt.text = r.label;
            roleSelection.appendChild(opt);
        });

        const loginButton = document.getElementById('submit-login');
        loginButton.value = 'Register';

        form.insertBefore(checkPwdInput, loginButton);
        form.insertBefore(emailInput, loginButton);
        form.insertBefore(roleSelection, loginButton);

        document.getElementById('home-form-container').style.marginTop = 0;
        document.getElementById('no-reg-link').style.display = 'none';
    });

    // Handle form submit request
    document.getElementById('submit-login').addEventListener('click', () => {  
        const form = document.getElementById('home-form');
        const submitButton = document.getElementById('submit-login');
        if (form.checkValidity()) {
            // valid form
            if (submitButton.value === 'Register') {
                // Register
                const validateEmail = email => {
                    const re = new RegExp(/^\w+([\.-]?\w+)+@\w+([\.:]?\w+)+(\.[a-zA-Z0-9]{2,})+$/);
                    return re.test(email);
                };
            
                const emailValue = document.getElementById('register-email').value;
                if (!validateEmail(emailValue)) {
                    alert('The email address is not valid, please try another one.');
                    return;
                };
            
                const pwd = document.getElementById('user-password').value;
                const checkPwd = document.getElementById('user-check-password').value;
            
                if (pwd.localeCompare(checkPwd) !== 0) {
                    alert('The password fields do not match, please retry.')
                    return;
                }
            
                fetch('Register', {
                    method: 'POST',
                    body: new URLSearchParams(new FormData(form))
                }).then(res => {
                    if (res.status === 409) {
                        alert('username already exists, choose another one.');
                        const usernameInput = document.getElementById('user-name');
                        usernameInput.value = '';
                        usernameInput.focus();
                    }
                    res.json().then(data => {
                        sessionStorage.setItem('user', JSON.stringify(data));
                        window.location.href = data.role.localeCompare('ROLE_CLIENT') === 0 ? 'HomeClient.html' : 'HomeEmployee.html';
                    });
                }).catch(err => {
                    alert('Something went wrong : ' + err);
                    form.reset();
                });
            } else {
                // Login
                fetch('CheckLogin', {
                    method: 'POST',
                    body: new URLSearchParams(new FormData(form))
                }).then(res => {
                    if (res.status === 401) {
                        alert('Incorrect credentials, retry.');
                        form.reset();
                    } else if (res.ok) {
                        res.json().then(data => {
                            sessionStorage.setItem('user', JSON.stringify(data));
                            window.location.href = data.role.localeCompare('ROLE_CLIENT') === 0 ? 'HomeClient.html' : 'HomeEmployee.html';
                        });
                    }
                }).catch(err => {
                    alert('Something went wrong : ' + err);
                    form.reset();
                });
            }
        } else {
            // Invalid form
            form.reportValidity();
        }
    });
})();