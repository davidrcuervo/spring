import { SingleInput, CheckBoxModal } from './forms/index.js';

new SingleInput(document.getElementById('companyNameForm'));
new SingleInput(document.getElementById('companyVanityUrlForm'));
new SingleInput(document.getElementById('companyOwnerForm'));

const allCheckBoxWithModal = document.querySelectorAll('form.checkBoxWithModal');

allCheckBoxWithModal.forEach((formWithCheckBoxModal) => {
    new CheckBoxModal(formWithCheckBoxModal);
});

document.querySelectorAll('div.modal').forEach((bootstrapModal) => {
    bootstrapModal.addEventListener('hide.bs.modal', () => {
        document.activeElement.blur();
    })
});