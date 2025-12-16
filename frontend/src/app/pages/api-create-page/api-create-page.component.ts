import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ApiContractService } from '../../services/api-contract.service';
import { Lifecycle } from '../../models/api-contract.model';

@Component({
  selector: 'app-api-create-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div>
      <h2>Create New API Contract</h2>
      
      <form [formGroup]="form" (ngSubmit)="onSubmit()" class="card">
        <div class="form-group">
          <label>Name *</label>
          <input type="text" formControlName="name" />
          <div class="error-message" *ngIf="form.get('name')?.hasError('required') && form.get('name')?.touched">
            Name is required
          </div>
          <div class="error-message" *ngIf="form.get('name')?.hasError('minlength') && form.get('name')?.touched">
            Name must be at least 2 characters
          </div>
          <div class="error-message" *ngIf="serverErrors.name">
            {{ serverErrors.name }}
          </div>
        </div>

        <div class="form-group">
          <label>Base URL *</label>
          <input type="text" formControlName="baseUrl" />
          <div class="error-message" *ngIf="form.get('baseUrl')?.hasError('required') && form.get('baseUrl')?.touched">
            Base URL is required
          </div>
          <div class="error-message" *ngIf="serverErrors.baseUrl">
            {{ serverErrors.baseUrl }}
          </div>
        </div>

        <div class="form-group">
          <label>Version *</label>
          <input type="text" formControlName="version" />
          <div class="error-message" *ngIf="form.get('version')?.hasError('required') && form.get('version')?.touched">
            Version is required
          </div>
          <div class="error-message" *ngIf="serverErrors.version">
            {{ serverErrors.version }}
          </div>
        </div>

        <div class="form-group">
          <label>Owner Team *</label>
          <input type="text" formControlName="ownerTeam" />
          <div class="error-message" *ngIf="form.get('ownerTeam')?.hasError('required') && form.get('ownerTeam')?.touched">
            Owner team is required
          </div>
          <div class="error-message" *ngIf="serverErrors.ownerTeam">
            {{ serverErrors.ownerTeam }}
          </div>
        </div>

        <div class="form-group">
          <label>Lifecycle *</label>
          <select formControlName="lifecycle">
            <option [value]="Lifecycle.DRAFT">DRAFT</option>
            <option [value]="Lifecycle.ACTIVE">ACTIVE</option>
            <option [value]="Lifecycle.DEPRECATED">DEPRECATED</option>
          </select>
          <div class="error-message" *ngIf="form.get('lifecycle')?.hasError('required') && form.get('lifecycle')?.touched">
            Lifecycle is required
          </div>
        </div>

        <div class="form-group">
          <label>OpenAPI URL</label>
          <input type="text" formControlName="openApiUrl" />
        </div>

        <div class="form-group">
          <label>Description</label>
          <textarea formControlName="description"></textarea>
        </div>

        <div style="display: flex; gap: 12px; margin-top: 20px;">
          <button type="submit" class="btn btn-primary" [disabled]="form.invalid || submitting">
            {{ submitting ? 'Creating...' : 'Create' }}
          </button>
          <button type="button" class="btn btn-secondary" (click)="cancel()">Cancel</button>
        </div>
      </form>
    </div>
  `
})
export class ApiCreatePageComponent {
  form: FormGroup;
  submitting = false;
  serverErrors: any = {};
  Lifecycle = Lifecycle;

  constructor(
    private fb: FormBuilder,
    private apiService: ApiContractService,
    private router: Router
  ) {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      baseUrl: ['', [Validators.required]],
      version: ['', [Validators.required]],
      ownerTeam: ['', [Validators.required]],
      lifecycle: [Lifecycle.DRAFT, [Validators.required]],
      openApiUrl: [''],
      description: ['']
    });
  }

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.serverErrors = {};

    this.apiService.create(this.form.value).subscribe({
      next: (api) => {
        this.router.navigate(['/apis', api.id]);
      },
      error: (err) => {
        this.submitting = false;
        if (err.error?.fieldErrors) {
          err.error.fieldErrors.forEach((fieldError: any) => {
            this.serverErrors[fieldError.field] = fieldError.message;
          });
        } else {
          this.serverErrors.general = err.error?.message || 'An error occurred';
        }
      }
    });
  }

  cancel() {
    this.router.navigate(['/apis']);
  }
}

