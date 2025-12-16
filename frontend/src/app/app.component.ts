import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <div class="container">
      <header style="margin-bottom: 30px; padding: 20px 0; border-bottom: 2px solid #007bff;">
        <h1 style="color: #007bff; font-size: 28px;">API Contract Registry</h1>
      </header>
      <router-outlet></router-outlet>
    </div>
  `
})
export class AppComponent {
  title = 'api-contract-registry';
}

