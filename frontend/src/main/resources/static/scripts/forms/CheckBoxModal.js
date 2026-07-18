export default class CheckBoxModal{
    constructor(formElement){
        this.form  = formElement;
        this.input = formElement.querySelector('input[type=checkbox]');
        this.modal = formElement.querySelector('div.modal');
        this.confirmButton = this.modal.querySelector('.modal-footer button.btn-primary');

        this.bootstrapModal = bootstrap.Modal.getInstance(this.modal) || new bootstrap.Modal(this.modal);
        this.bindEvents();
    }

    bindEvents(){
        this.input.addEventListener('change', () => this.onChange());
        this.modal.addEventListener('hide.bs.modal', () => this.onHideModal());
        this.modal.addEventListener('hidden.bs.modal', () => this.onCloseModal());
        this.confirmButton.addEventListener('click', () => this.onConfirmButton());
    }

    onChange(){
        this.bootstrapModal.show();
    }

    onHideModal(){
        document.activeElement.blur();
    }

    onCloseModal(){
        this.input.checked = !this.input.checked;
    }

    onConfirmButton(){
        this.form.submit();
    }
}