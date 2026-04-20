import { Routes } from '@angular/router';

export const appRoutes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'contracts',
  },
  {
    path: 'contracts',
    loadChildren: () =>
      import('./features/contracts/contracts.routes').then((m) => m.contractsRoutes),
  },
];
