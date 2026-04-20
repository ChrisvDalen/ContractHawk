import { Routes } from '@angular/router';

export const contractsRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/contracts-overview-page/contracts-overview-page.component').then(
        (m) => m.ContractsOverviewPageComponent,
      ),
  },
  {
    path: 'upload',
    loadComponent: () =>
      import('./pages/contract-upload-page/contract-upload-page.component').then(
        (m) => m.ContractUploadPageComponent,
      ),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./pages/contract-detail-page/contract-detail-page.component').then(
        (m) => m.ContractDetailPageComponent,
      ),
  },
];
