Why the code in @frontend/src/main/resources/templates/Root/mock.html generates double label when it is expected only label:

```html
<!-- Update Company name -->
        <form class="mx-1"
              action="/manage/company/{companyId}/update/name?redirect=/manage/company/{companyId}"
              method="post"
              id="companyNameForm"
        >
            
    <div class="row">
        
    <label for="companyNameFormInput"
           class="col-12 col-md-2 col-form-label text-md-end"
    >
        <span>Company Name</span>
    </label>
<label for="companyNameFormInput"
           class="form-label"
    >Company Nameaslkasdfasdf</label>
```