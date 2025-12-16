import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiContractService } from '../../services/api-contract.service';
import { ApiSummary, Lifecycle, ApiListParams } from '../../models/api-contract.model';

@Component({
  selector: 'app-api-list-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div>
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
        <h2>API Contracts</h2>
        <button class="btn btn-primary" (click)="navigateToCreate()">New API</button>
      </div>

      <div class="search-filters">
        <input 
          type="text" 
          placeholder="Search..." 
          [(ngModel)]="searchQuery"
          (input)="onSearchChange()"
          style="flex: 1; min-width: 200px;">
        
        <select [(ngModel)]="selectedLifecycle" (change)="loadApis()" style="width: 150px;">
          <option value="">All Lifecycles</option>
          <option [value]="Lifecycle.DRAFT">DRAFT</option>
          <option [value]="Lifecycle.ACTIVE">ACTIVE</option>
          <option [value]="Lifecycle.DEPRECATED">DEPRECATED</option>
        </select>
        
        <input 
          type="text" 
          placeholder="Owner Team" 
          [(ngModel)]="ownerTeamFilter"
          (input)="onSearchChange()"
          style="width: 150px;">
        
        <select [(ngModel)]="sortField" (change)="loadApis()" style="width: 150px;">
          <option value="updatedAt">Sort by Updated</option>
          <option value="name">Sort by Name</option>
          <option value="ownerTeam">Sort by Team</option>
          <option value="lifecycle">Sort by Lifecycle</option>
        </select>
        
        <select [(ngModel)]="sortDir" (change)="loadApis()" style="width: 100px;">
          <option value="desc">Desc</option>
          <option value="asc">Asc</option>
        </select>
      </div>

      <table class="table" *ngIf="apis.length > 0">
        <thead>
          <tr>
            <th>Name</th>
            <th>Version</th>
            <th>Owner Team</th>
            <th>Lifecycle</th>
            <th>Updated At</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let api of apis" (click)="navigateToDetail(api.id)">
            <td><strong>{{ api.name }}</strong></td>
            <td>{{ api.version }}</td>
            <td>{{ api.ownerTeam }}</td>
            <td>
              <span class="badge" [ngClass]="'badge-' + api.lifecycle.toLowerCase()">
                {{ api.lifecycle }}
              </span>
            </td>
            <td>{{ formatDate(api.updatedAt) }}</td>
          </tr>
        </tbody>
      </table>

      <div *ngIf="apis.length === 0 && !loading" class="card">
        <p>No API contracts found.</p>
      </div>

      <div *ngIf="loading" class="card">
        <p>Loading...</p>
      </div>
    </div>
  `
})
export class ApiListPageComponent implements OnInit {
  apis: ApiSummary[] = [];
  loading = false;
  searchQuery = '';
  selectedLifecycle: Lifecycle | '' = '';
  ownerTeamFilter = '';
  sortField = 'updatedAt';
  sortDir = 'desc';
  Lifecycle = Lifecycle;
  private searchTimeout: any;

  constructor(
    private apiService: ApiContractService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadApis();
  }

  onSearchChange() {
    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout);
    }
    this.searchTimeout = setTimeout(() => {
      this.loadApis();
    }, 300);
  }

  loadApis() {
    this.loading = true;
    const params: ApiListParams = {
      q: this.searchQuery || undefined,
      lifecycle: this.selectedLifecycle || undefined,
      ownerTeam: this.ownerTeamFilter || undefined,
      sort: this.sortField,
      dir: this.sortDir
    };

    this.apiService.list(params).subscribe({
      next: (data) => {
        this.apis = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading APIs:', err);
        this.loading = false;
      }
    });
  }

  navigateToDetail(id: string) {
    this.router.navigate(['/apis', id]);
  }

  navigateToCreate() {
    this.router.navigate(['/apis/new']);
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString();
  }
}

