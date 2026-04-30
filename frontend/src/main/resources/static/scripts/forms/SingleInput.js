export default class SingleInput {
    constructor(formEl) {
        this.form = formEl;
        this.input = formEl.querySelector('input');
        this.editBtn = formEl.querySelector('button.btn-outline-primary');
        this.saveBtn = formEl.querySelector('button.btn-outline-success');
        this.cancelBtn = formEl.querySelector('button.btn-outline-danger');
        this.value = this.input.value;
        this.bindEvents();
    }

    bindEvents(){
        this.editBtn.addEventListener('click', () => this.onEdit());
        this.cancelBtn.addEventListener('click', () => this.onCancel());
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
}