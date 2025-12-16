import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/apis',
    pathMatch: 'full'
  },
  {
    path: 'apis',
    loadComponent: () => import('./pages/api-list-page/api-list-page.component').then(m => m.ApiListPageComponent)
  },
  {
    path: 'apis/new',
    loadComponent: () => import('./pages/api-create-page/api-create-page.component').then(m => m.ApiCreatePageComponent)
  },
  {
    path: 'apis/:id',
    loadComponent: () => import('./pages/api-detail-page/api-detail-page.component').then(m => m.ApiDetailPageComponent)
  }
];

