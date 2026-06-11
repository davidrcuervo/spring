export default class SingleInput {
    constructor(formEl) {
        this.form = formEl;
        let formId = formEl.id;
        let inputId = formId + 'Input';
        let modalId = inputId + 'Modal';
        console.log("inputId: " + inputId);
        this.input = formEl.querySelector('input#' + inputId)
                        ?? formEl.querySelector('select#' + inputId);
        this.editBtn = formEl.querySelector('button.btn-outline-primary');
        this.saveBtn = formEl.querySelector('button.btn-outline-success');
        this.cancelBtn = formEl.querySelector('button.btn-outline-danger');
        this.confirmModalBtn = formEl.querySelector('#' + modalId + ' .modal-footer button.btn-primary');
        this.value = this.input.value;
        this.bindEvents();
    }

    bindEvents(){
        this.editBtn.addEventListener('click', () => this.onEdit());
        this.cancelBtn.addEventListener('click', () => this.onCancel());
        this.confirmModalBtn.addEventListener('click', () => this.onConfirmModal());
        this.form.addEventListener('submit', (e) => this.onSubmit(e));
    }

    onEdit() {
            this.input.removeAttribute('readonly');
            this.input.focus();
            this.editBtn.classList.add('d-none');
            this.saveBtn.classList.remove('d-none');
            this.cancelBtn.classList.remove('d-none')
    }

    onCancel() {
            this.input.setAttribute('readonly', true);
            this.editBtn.classList.remove('d-none');
            this.saveBtn.classList.add('d-none');
            this.cancelBtn.classList.add('d-none');
            this.input.value = this.value;
    }

    onSubmit(e){
        e.preventDefault();
        console.log('Submitting company update name form');
    }

    onConfirmModal(){
        this.form.submit();
    }
}