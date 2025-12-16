import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ApiContractService } from '../../services/api-contract.service';
import {
  ApiDetail,
  Lifecycle,
  HttpMethod,
  ChangelogType,
  EndpointCreateDto,
  ChangelogCreateDto
} from '../../models/api-contract.model';

@Component({
  selector: 'app-api-detail-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div *ngIf="api">
      <div class="card-header">
        <div>
          <h2>{{ api.name }} <span class="badge" [ngClass]="'badge-' + api.lifecycle.toLowerCase()">{{ api.lifecycle }}</span></h2>
          <p style="color: #666; margin-top: 4px;">{{ api.baseUrl }} - v{{ api.version }}</p>
        </div>
        <div style="display: flex; gap: 8px;">
          <button class="btn btn-secondary" (click)="toggleEdit()">{{ editing ? 'Cancel' : 'Edit' }}</button>
          <button class="btn btn-success" (click)="updateLifecycle(Lifecycle.DRAFT)">DRAFT</button>
          <button class="btn btn-success" (click)="updateLifecycle(Lifecycle.ACTIVE)">ACTIVE</button>
          <button class="btn btn-secondary" (click)="updateLifecycle(Lifecycle.DEPRECATED)">DEPRECATED</button>
          <button class="btn btn-danger" (click)="deleteApi()">Delete</button>
        </div>
      </div>

      <div class="card" *ngIf="!editing">
        <h3>Overview</h3>
        <p><strong>Owner Team:</strong> {{ api.ownerTeam }}</p>
        <p *ngIf="api.openApiUrl"><strong>OpenAPI URL:</strong> <a [href]="api.openApiUrl" target="_blank">{{ api.openApiUrl }}</a></p>
        <p *ngIf="api.description"><strong>Description:</strong> {{ api.description }}</p>
        <p><strong>Created:</strong> {{ formatDate(api.createdAt) }}</p>
        <p><strong>Updated:</strong> {{ formatDate(api.updatedAt) }}</p>
      </div>

      <div class="card" *ngIf="editing">
        <h3>Edit API</h3>
        <form [formGroup]="editForm" (ngSubmit)="onUpdate()">
          <div class="form-group">
            <label>Name *</label>
            <input type="text" formControlName="name" />
          </div>
          <div class="form-group">
            <label>Base URL *</label>
            <input type="text" formControlName="baseUrl" />
          </div>
          <div class="form-group">
            <label>Version *</label>
            <input type="text" formControlName="version" />
          </div>
          <div class="form-group">
            <label>Owner Team *</label>
            <input type="text" formControlName="ownerTeam" />
          </div>
          <div class="form-group">
            <label>OpenAPI URL</label>
            <input type="text" formControlName="openApiUrl" />
          </div>
          <div class="form-group">
            <label>Description</label>
            <textarea formControlName="description"></textarea>
          </div>
          <button type="submit" class="btn btn-primary" [disabled]="editForm.invalid || updating">Save</button>
        </form>
      </div>

      <div class="tabs">
        <button class="tab" [class.active]="activeTab === 'endpoints'" (click)="activeTab = 'endpoints'">Endpoints</button>
        <button class="tab" [class.active]="activeTab === 'changelog'" (click)="activeTab = 'changelog'">Changelog</button>
      </div>

      <div class="tab-content" *ngIf="activeTab === 'endpoints'">
        <div class="card">
          <h3>Add Endpoint</h3>
          <form [formGroup]="endpointForm" (ngSubmit)="addEndpoint()">
            <div style="display: grid; grid-template-columns: 150px 1fr 1fr auto; gap: 12px; align-items: end;">
              <div class="form-group">
                <label>Method *</label>
                <select formControlName="method">
                  <option [value]="HttpMethod.GET">GET</option>
                  <option [value]="HttpMethod.POST">POST</option>
                  <option [value]="HttpMethod.PUT">PUT</option>
                  <option [value]="HttpMethod.PATCH">PATCH</option>
                  <option [value]="HttpMethod.DELETE">DELETE</option>
                </select>
              </div>
              <div class="form-group">
                <label>Path *</label>
                <input type="text" formControlName="path" placeholder="/users/{id}" />
              </div>
              <div class="form-group">
                <label>Description</label>
                <input type="text" formControlName="description" />
              </div>
              <div class="form-group">
                <label>
                  <input type="checkbox" formControlName="deprecated" />
                  Deprecated
                </label>
              </div>
            </div>
            <button type="submit" class="btn btn-primary" [disabled]="endpointForm.invalid">Add</button>
          </form>
        </div>

        <div *ngFor="let endpoint of api.endpoints" class="endpoint-item">
          <div class="endpoint-item-header">
            <div>
              <span class="endpoint-method" [ngClass]="'method-' + endpoint.method.toLowerCase()">{{ endpoint.method }}</span>
              <strong>{{ endpoint.path }}</strong>
              <span *ngIf="endpoint.deprecated" class="badge badge-deprecated">Deprecated</span>
            </div>
            <div>
              <button class="btn btn-secondary" (click)="editEndpoint(endpoint)" *ngIf="!editingEndpoint || editingEndpoint.id !== endpoint.id">Edit</button>
              <button class="btn btn-danger" (click)="deleteEndpoint(endpoint.id)">Delete</button>
            </div>
          </div>
          <p *ngIf="endpoint.description">{{ endpoint.description }}</p>
          <p style="font-size: 12px; color: #666;">Created: {{ formatDate(endpoint.createdAt) }}</p>
          
          <div *ngIf="editingEndpoint && editingEndpoint.id === endpoint.id" class="card" style="margin-top: 12px;">
            <form [formGroup]="endpointEditForm" (ngSubmit)="updateEndpoint(endpoint.id)">
              <div style="display: grid; grid-template-columns: 150px 1fr 1fr auto; gap: 12px;">
                <div class="form-group">
                  <label>Method *</label>
                  <select formControlName="method">
                    <option [value]="HttpMethod.GET">GET</option>
                    <option [value]="HttpMethod.POST">POST</option>
                    <option [value]="HttpMethod.PUT">PUT</option>
                    <option [value]="HttpMethod.PATCH">PATCH</option>
                    <option [value]="HttpMethod.DELETE">DELETE</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>Path *</label>
                  <input type="text" formControlName="path" />
                </div>
                <div class="form-group">
                  <label>Description</label>
                  <input type="text" formControlName="description" />
                </div>
                <div class="form-group">
                  <label>
                    <input type="checkbox" formControlName="deprecated" />
                    Deprecated
                  </label>
                </div>
              </div>
              <button type="submit" class="btn btn-primary">Save</button>
              <button type="button" class="btn btn-secondary" (click)="cancelEndpointEdit()">Cancel</button>
            </form>
          </div>
        </div>
      </div>

      <div class="tab-content" *ngIf="activeTab === 'changelog'">
        <div class="card">
          <h3>Add Changelog Entry</h3>
          <form [formGroup]="changelogForm" (ngSubmit)="addChangelogEntry()">
            <div class="form-group">
              <label>Type *</label>
              <select formControlName="type">
                <option [value]="ChangelogType.ADDED">ADDED</option>
                <option [value]="ChangelogType.CHANGED">CHANGED</option>
                <option [value]="ChangelogType.DEPRECATED">DEPRECATED</option>
                <option [value]="ChangelogType.REMOVED">REMOVED</option>
                <option [value]="ChangelogType.FIXED">FIXED</option>
              </select>
            </div>
            <div class="form-group">
              <label>
                <input type="checkbox" formControlName="breaking" />
                Breaking Change
              </label>
            </div>
            <div class="form-group">
              <label>Summary *</label>
              <input type="text" formControlName="summary" />
            </div>
            <div class="form-group">
              <label>Details</label>
              <textarea formControlName="details"></textarea>
            </div>
            <div class="form-group">
              <label>Released At *</label>
              <input type="datetime-local" formControlName="releasedAt" />
            </div>
            <button type="submit" class="btn btn-primary" [disabled]="changelogForm.invalid">Add</button>
          </form>
        </div>

        <div *ngFor="let entry of api.changelog" class="changelog-item">
          <div class="changelog-item-header">
            <div>
              <strong>{{ entry.type }}</strong>
              <span *ngIf="entry.breaking" class="badge badge-breaking">Breaking</span>
              <span style="color: #666; margin-left: 8px;">{{ formatDate(entry.releasedAt) }}</span>
            </div>
            <button class="btn btn-danger" (click)="deleteChangelogEntry(entry.id)">Delete</button>
          </div>
          <p><strong>{{ entry.summary }}</strong></p>
          <p *ngIf="entry.details">{{ entry.details }}</p>
        </div>
      </div>
    </div>

    <div *ngIf="loading" class="card">
      <p>Loading...</p>
    </div>
  `
})
export class ApiDetailPageComponent implements OnInit {
  api: ApiDetail | null = null;
  loading = true;
  editing = false;
  activeTab: 'endpoints' | 'changelog' = 'endpoints';
  editingEndpoint: any = null;
  updating = false;

  Lifecycle = Lifecycle;
  HttpMethod = HttpMethod;
  ChangelogType = ChangelogType;

  editForm: FormGroup;
  endpointForm: FormGroup;
  endpointEditForm: FormGroup;
  changelogForm: FormGroup;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private apiService: ApiContractService,
    private fb: FormBuilder
  ) {
    this.editForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      baseUrl: ['', [Validators.required]],
      version: ['', [Validators.required]],
      ownerTeam: ['', [Validators.required]],
      openApiUrl: [''],
      description: ['']
    });

    this.endpointForm = this.fb.group({
      method: [HttpMethod.GET, [Validators.required]],
      path: ['', [Validators.required]],
      description: [''],
      deprecated: [false]
    });

    this.endpointEditForm = this.fb.group({
      method: [HttpMethod.GET, [Validators.required]],
      path: ['', [Validators.required]],
      description: [''],
      deprecated: [false]
    });

    this.changelogForm = this.fb.group({
      type: [ChangelogType.ADDED, [Validators.required]],
      breaking: [false],
      summary: ['', [Validators.required]],
      details: [''],
      releasedAt: ['', [Validators.required]]
    });
  }

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadApi(id);
    }
  }

  loadApi(id: string) {
    this.loading = true;
    this.apiService.get(id).subscribe({
      next: (api) => {
        this.api = api;
        this.loading = false;
        this.populateEditForm();
      },
      error: (err) => {
        console.error('Error loading API:', err);
        this.loading = false;
      }
    });
  }

  populateEditForm() {
    if (this.api) {
      this.editForm.patchValue({
        name: this.api.name,
        baseUrl: this.api.baseUrl,
        version: this.api.version,
        ownerTeam: this.api.ownerTeam,
        openApiUrl: this.api.openApiUrl || '',
        description: this.api.description || ''
      });
    }
  }

  toggleEdit() {
    this.editing = !this.editing;
    if (this.editing) {
      this.populateEditForm();
    }
  }

  onUpdate() {
    if (this.editForm.invalid || !this.api) return;

    this.updating = true;
    const updateDto = {
      ...this.editForm.value,
      lifecycle: this.api.lifecycle
    };

    this.apiService.update(this.api.id, updateDto).subscribe({
      next: (updated) => {
        this.api = updated;
        this.editing = false;
        this.updating = false;
      },
      error: (err) => {
        console.error('Error updating API:', err);
        this.updating = false;
      }
    });
  }

  updateLifecycle(lifecycle: Lifecycle) {
    if (!this.api) return;
    this.apiService.patchLifecycle(this.api.id, lifecycle).subscribe({
      next: () => {
        if (this.api) {
          this.api.lifecycle = lifecycle;
        }
      },
      error: (err) => {
        console.error('Error updating lifecycle:', err);
      }
    });
  }

  deleteApi() {
    if (!this.api || !confirm('Are you sure you want to delete this API contract?')) return;
    
    this.apiService.delete(this.api.id).subscribe({
      next: () => {
        this.router.navigate(['/apis']);
      },
      error: (err) => {
        console.error('Error deleting API:', err);
      }
    });
  }

  addEndpoint() {
    if (this.endpointForm.invalid || !this.api) return;

    this.apiService.addEndpoint(this.api.id, this.endpointForm.value).subscribe({
      next: (endpoint) => {
        this.api!.endpoints.push(endpoint);
        this.endpointForm.reset({
          method: HttpMethod.GET,
          path: '',
          description: '',
          deprecated: false
        });
      },
      error: (err) => {
        console.error('Error adding endpoint:', err);
        alert(err.error?.message || 'Error adding endpoint');
      }
    });
  }

  editEndpoint(endpoint: any) {
    this.editingEndpoint = endpoint;
    this.endpointEditForm.patchValue({
      method: endpoint.method,
      path: endpoint.path,
      description: endpoint.description || '',
      deprecated: endpoint.deprecated
    });
  }

  cancelEndpointEdit() {
    this.editingEndpoint = null;
  }

  updateEndpoint(endpointId: string) {
    if (this.endpointEditForm.invalid || !this.api) return;

    this.apiService.updateEndpoint(this.api.id, endpointId, this.endpointEditForm.value).subscribe({
      next: (updated) => {
        const index = this.api!.endpoints.findIndex(e => e.id === endpointId);
        if (index !== -1) {
          this.api!.endpoints[index] = updated;
        }
        this.editingEndpoint = null;
      },
      error: (err) => {
        console.error('Error updating endpoint:', err);
        alert(err.error?.message || 'Error updating endpoint');
      }
    });
  }

  deleteEndpoint(endpointId: string) {
    if (!this.api || !confirm('Are you sure you want to delete this endpoint?')) return;

    this.apiService.deleteEndpoint(this.api.id, endpointId).subscribe({
      next: () => {
        this.api!.endpoints = this.api!.endpoints.filter(e => e.id !== endpointId);
      },
      error: (err) => {
        console.error('Error deleting endpoint:', err);
      }
    });
  }

  addChangelogEntry() {
    if (this.changelogForm.invalid || !this.api) return;

    const dto: ChangelogCreateDto = {
      ...this.changelogForm.value,
      releasedAt: new Date(this.changelogForm.value.releasedAt).toISOString()
    };

    this.apiService.addChangelogEntry(this.api.id, dto).subscribe({
      next: (entry) => {
        this.api!.changelog.unshift(entry);
        this.changelogForm.reset({
          type: ChangelogType.ADDED,
          breaking: false,
          summary: '',
          details: '',
          releasedAt: ''
        });
      },
      error: (err) => {
        console.error('Error adding changelog entry:', err);
        alert(err.error?.message || 'Error adding changelog entry');
      }
    });
  }

  deleteChangelogEntry(entryId: string) {
    if (!this.api || !confirm('Are you sure you want to delete this changelog entry?')) return;

    this.apiService.deleteChangelogEntry(this.api.id, entryId).subscribe({
      next: () => {
        this.api!.changelog = this.api!.changelog.filter(e => e.id !== entryId);
      },
      error: (err) => {
        console.error('Error deleting changelog entry:', err);
      }
    });
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString();
  }
}

