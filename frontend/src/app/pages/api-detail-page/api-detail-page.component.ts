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
  ChangelogCreateDto,
  SyncMode,
  Diff,
  ImportResult,
  ApiSyncRun
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
        <button class="tab" [class.active]="activeTab === 'openapi'" (click)="activeTab = 'openapi'" *ngIf="api.openApiUrl">OpenAPI Sync</button>
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

      <div class="tab-content" *ngIf="activeTab === 'openapi' && api.openApiUrl">
        <div class="card">
          <h3>OpenAPI Sync</h3>
          <p><strong>OpenAPI URL:</strong> <a [href]="api.openApiUrl" target="_blank">{{ api.openApiUrl }}</a></p>
          
          <div style="display: flex; gap: 12px; margin: 20px 0;">
            <button class="btn btn-primary" (click)="previewDiff()" [disabled]="loadingDiff">
              {{ loadingDiff ? 'Loading...' : 'Preview Diff' }}
            </button>
            <button class="btn btn-success" (click)="importOpenApi(SyncMode.MERGE)" [disabled]="importing">
              {{ importing ? 'Importing...' : 'Import (Merge)' }}
            </button>
            <button class="btn btn-warning" (click)="confirmReplaceImport()" [disabled]="importing">
              {{ importing ? 'Importing...' : 'Import (Replace)' }}
            </button>
          </div>

          <div *ngIf="diffError" class="error-message" style="margin: 12px 0; padding: 12px; background: #fee; border: 1px solid #fcc;">
            <strong>Error:</strong> {{ diffError }}
          </div>

          <div *ngIf="diff && !loadingDiff" class="card" style="margin-top: 20px;">
            <h4>Preview Changes</h4>
            
            <div *ngIf="diff.addedEndpoints.length > 0" style="margin-bottom: 20px;">
              <h5 style="color: #28a745;">Added Endpoints ({{ diff.addedEndpoints.length }})</h5>
              <div *ngFor="let ep of diff.addedEndpoints" class="endpoint-item" style="background: #d4edda;">
                <span class="endpoint-method" [ngClass]="'method-' + ep.method.toLowerCase()">{{ ep.method }}</span>
                <strong>{{ ep.path }}</strong>
                <p *ngIf="ep.description" style="margin-top: 4px;">{{ ep.description }}</p>
              </div>
            </div>

            <div *ngIf="diff.removedEndpoints.length > 0" style="margin-bottom: 20px;">
              <h5 style="color: #dc3545;">Removed Endpoints ({{ diff.removedEndpoints.length }}) <span class="badge badge-breaking">Breaking</span></h5>
              <div *ngFor="let ep of diff.removedEndpoints" class="endpoint-item" style="background: #f8d7da;">
                <span class="endpoint-method" [ngClass]="'method-' + ep.method.toLowerCase()">{{ ep.method }}</span>
                <strong>{{ ep.path }}</strong>
                <p *ngIf="ep.description" style="margin-top: 4px;">{{ ep.description }}</p>
              </div>
            </div>

            <div *ngIf="diff.changedEndpoints.length > 0" style="margin-bottom: 20px;">
              <h5 style="color: #ffc107;">Changed Endpoints ({{ diff.changedEndpoints.length }})</h5>
              <div *ngFor="let change of diff.changedEndpoints" class="endpoint-item" style="background: #fff3cd;">
                <span class="endpoint-method" [ngClass]="'method-' + change.current.method.toLowerCase()">{{ change.current.method }}</span>
                <strong>{{ change.current.path }}</strong>
                <p style="margin-top: 4px; font-size: 12px; color: #856404;">{{ change.changeDescription }}</p>
              </div>
            </div>

            <div *ngIf="diff.addedEndpoints.length === 0 && diff.removedEndpoints.length === 0 && diff.changedEndpoints.length === 0" style="padding: 20px; text-align: center; color: #666;">
              No changes detected. Endpoints are in sync with OpenAPI spec.
            </div>
          </div>

          <div *ngIf="importResult" class="card" style="margin-top: 20px; background: #d1ecf1; border: 1px solid #bee5eb;">
            <h4>Import Result</h4>
            <p><strong>Added:</strong> {{ importResult.addedCount }} | <strong>Updated:</strong> {{ importResult.updatedCount }} | <strong>Deleted:</strong> {{ importResult.deletedCount }}</p>
            <div *ngIf="importResult.breaksDetected" style="margin-top: 12px;">
              <span class="badge badge-breaking">Breaking Changes Detected!</span>
              <div *ngFor="let bc of importResult.breakingChanges" style="margin-top: 8px; padding: 8px; background: #f8d7da; border-radius: 4px;">
                <strong>{{ bc.type }}</strong>: {{ bc.method }} {{ bc.path }}
                <p *ngIf="bc.details" style="font-size: 12px; margin-top: 4px;">{{ bc.details }}</p>
              </div>
            </div>
            <button class="btn btn-primary" (click)="loadApi(api.id)" style="margin-top: 12px;">Refresh Page</button>
          </div>
        </div>

        <div class="card" style="margin-top: 20px;">
          <h3>Sync History</h3>
          <div *ngIf="loadingSyncRuns">Loading sync history...</div>
          <table class="table" *ngIf="syncRuns.length > 0 && !loadingSyncRuns">
            <thead>
              <tr>
                <th>Run At</th>
                <th>Status</th>
                <th>Mode</th>
                <th>Changes</th>
                <th>Breaking</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let run of syncRuns.slice(0, 10)" (click)="viewSyncRunDetails(run)" style="cursor: pointer;">
                <td>{{ formatDate(run.runAt) }}</td>
                <td>
                  <span class="badge" [ngClass]="run.status === 'SUCCESS' ? 'badge-active' : 'badge-deprecated'">
                    {{ run.status }}
                  </span>
                </td>
                <td>{{ run.mode }}</td>
                <td>+{{ run.addedCount }} ~{{ run.updatedCount }} -{{ run.deletedCount }}</td>
                <td>
                  <span *ngIf="run.breaksDetected" class="badge badge-breaking">Yes</span>
                  <span *ngIf="!run.breaksDetected">-</span>
                </td>
              </tr>
            </tbody>
          </table>
          <div *ngIf="syncRuns.length === 0 && !loadingSyncRuns" style="padding: 20px; text-align: center; color: #666;">
            No sync runs yet.
          </div>
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
  activeTab: 'endpoints' | 'changelog' | 'openapi' = 'endpoints';
  editingEndpoint: any = null;
  updating = false;
  loadingDiff = false;
  importing = false;
  diff: Diff | null = null;
  diffError: string | null = null;
  importResult: ImportResult | null = null;
  syncRuns: ApiSyncRun[] = [];
  loadingSyncRuns = false;

  Lifecycle = Lifecycle;
  HttpMethod = HttpMethod;
  ChangelogType = ChangelogType;
  SyncMode = SyncMode;

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
        if (api.openApiUrl) {
          this.loadSyncRuns(id);
        }
      },
      error: (err) => {
        console.error('Error loading API:', err);
        this.loading = false;
      }
    });
  }

  loadSyncRuns(apiId: string) {
    this.loadingSyncRuns = true;
    this.apiService.getSyncRuns(apiId).subscribe({
      next: (runs) => {
        this.syncRuns = runs;
        this.loadingSyncRuns = false;
      },
      error: (err) => {
        console.error('Error loading sync runs:', err);
        this.loadingSyncRuns = false;
      }
    });
  }

  previewDiff() {
    if (!this.api) return;
    this.loadingDiff = true;
    this.diffError = null;
    this.diff = null;
    
    this.apiService.previewOpenApiDiff(this.api.id).subscribe({
      next: (diff) => {
        this.diff = diff;
        this.loadingDiff = false;
      },
      error: (err) => {
        this.loadingDiff = false;
        if (err.error?.message) {
          this.diffError = err.error.message;
        } else if (err.message) {
          this.diffError = err.message;
        } else {
          this.diffError = 'Failed to fetch OpenAPI spec. Check if the URL is accessible.';
        }
      }
    });
  }

  importOpenApi(mode: SyncMode) {
    if (!this.api) return;
    this.importing = true;
    this.importResult = null;
    
    this.apiService.importOpenApi(this.api.id, mode).subscribe({
      next: (result) => {
        this.importResult = result;
        this.importing = false;
        this.loadSyncRuns(this.api!.id);
        // Reload API to refresh endpoints
        setTimeout(() => this.loadApi(this.api!.id), 1000);
      },
      error: (err) => {
        this.importing = false;
        const errorMsg = err.error?.message || err.message || 'Failed to import OpenAPI';
        alert('Import failed: ' + errorMsg);
      }
    });
  }

  confirmReplaceImport() {
    if (!confirm('Replace mode will DELETE endpoints that are not in the OpenAPI spec. This may cause breaking changes. Continue?')) {
      return;
    }
    this.importOpenApi(SyncMode.REPLACE);
  }

  viewSyncRunDetails(run: ApiSyncRun) {
    // For now, just show alert. Could navigate to detail page later
    let details = `Sync Run: ${this.formatDate(run.runAt)}\n`;
    details += `Status: ${run.status}\n`;
    details += `Mode: ${run.mode}\n`;
    details += `Added: ${run.addedCount}, Updated: ${run.updatedCount}, Deleted: ${run.deletedCount}\n`;
    if (run.breaksDetected && run.breakingChanges.length > 0) {
      details += `\nBreaking Changes:\n`;
      run.breakingChanges.forEach(bc => {
        details += `- ${bc.type}: ${bc.method} ${bc.path}\n`;
      });
    }
    if (run.errorMessage) {
      details += `\nError: ${run.errorMessage}`;
    }
    alert(details);
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

