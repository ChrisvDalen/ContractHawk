import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiCreatePageComponent } from './api-create-page.component';
import { ApiContractService } from '../../services/api-contract.service';
import { of, throwError } from 'rxjs';

describe('ApiCreatePageComponent', () => {
  let component: ApiCreatePageComponent;
  let fixture: ComponentFixture<ApiCreatePageComponent>;
  let apiService: jasmine.SpyObj<ApiContractService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    const apiServiceSpy = jasmine.createSpyObj('ApiContractService', ['create']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [ApiCreatePageComponent, ReactiveFormsModule],
      providers: [
        { provide: ApiContractService, useValue: apiServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ApiCreatePageComponent);
    component = fixture.componentInstance;
    apiService = TestBed.inject(ApiContractService) as jasmine.SpyObj<ApiContractService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should disable submit when form is invalid', () => {
    fixture.detectChanges();
    expect(component.form.invalid).toBe(true);
    expect(component.form.get('name')?.hasError('required')).toBe(true);
  });

  it('should show validation errors when form is touched', () => {
    fixture.detectChanges();
    component.form.get('name')?.markAsTouched();
    fixture.detectChanges();
    
    const compiled = fixture.nativeElement;
    const errorMessage = compiled.querySelector('.error-message');
    expect(errorMessage).toBeTruthy();
  });

  it('should call service create on valid submit', () => {
    const mockApi = { id: '1', name: 'Test API' } as any;
    apiService.create.and.returnValue(of(mockApi));

    component.form.patchValue({
      name: 'Test API',
      baseUrl: 'https://api.example.com',
      version: 'v1.0.0',
      ownerTeam: 'Team A',
      lifecycle: 'DRAFT'
    });

    component.onSubmit();
    
    expect(apiService.create).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/apis', '1']);
  });

  it('should display server errors on create failure', () => {
    const errorResponse = {
      error: {
        fieldErrors: [
          { field: 'name', message: 'Name already exists' }
        ]
      }
    };
    apiService.create.and.returnValue(throwError(() => errorResponse));

    component.form.patchValue({
      name: 'Test API',
      baseUrl: 'https://api.example.com',
      version: 'v1.0.0',
      ownerTeam: 'Team A',
      lifecycle: 'DRAFT'
    });

    component.onSubmit();
    
    expect(component.serverErrors.name).toBe('Name already exists');
  });
});

