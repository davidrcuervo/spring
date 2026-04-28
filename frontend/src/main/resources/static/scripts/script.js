document.addEventListener('DOMContentLoaded', () => {

    const companyNameInput = document.getElementById('companyNameInput');
    const companyNameEditBtn = document.getElementById('companyNameInputEditBtn');
    const companyNameSaveBtn = document.getElementById('companyNameInputSaveBtn');
    const companyNameCancelBtn = document.getElementById('companyNameInputCancelBtn');
    const companyNameValue = document.getElementById('companyNameInput').value;

    companyNameEditBtn.addEventListener('click', () => {
        companyNameInput.removeAttribute('readonly');
        companyNameInput.focus();
        companyNameEditBtn.classList.add('d-none');
        companyNameSaveBtn.classList.remove('d-none');
        companyNameCancelBtn.classList.remove('d-none')
    });

    companyNameCancelBtn.addEventListener('click', () => {
        companyNameInput.setAttribute('readonly', true);
        companyNameEditBtn.classList.remove('d-none');
        companyNameSaveBtn.classList.add('d-none');
        companyNameCancelBtn.classList.add('d-none');
        companyNameInput.value = companyNameValue;
    });

    document.getElementById('companyNameForm').addEventListener('submit', (e) => {
        e.preventDefault();
        console.log('Submitting company update name form');
    });
});