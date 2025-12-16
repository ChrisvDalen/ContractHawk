import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ApiContractService } from './api-contract.service';
import { environment } from '../environments/environment';
import { ApiSummary, ApiDetail, Lifecycle, HttpMethod } from '../models/api-contract.model';

describe('ApiContractService', () => {
  let service: ApiContractService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ApiContractService]
    });
    service = TestBed.inject(ApiContractService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch list of APIs', () => {
    const mockApis: ApiSummary[] = [
      {
        id: '1',
        name: 'Test API',
        baseUrl: 'https://api.example.com',
        version: 'v1.0.0',
        ownerTeam: 'Team A',
        lifecycle: Lifecycle.ACTIVE,
        updatedAt: '2024-01-01T00:00:00Z'
      }
    ];

    service.list().subscribe(apis => {
      expect(apis.length).toBe(1);
      expect(apis[0].name).toBe('Test API');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/api/apis`);
    expect(req.request.method).toBe('GET');
    req.flush(mockApis);
  });

  it('should fetch API by id', () => {
    const mockApi: ApiDetail = {
      id: '1',
      name: 'Test API',
      baseUrl: 'https://api.example.com',
      version: 'v1.0.0',
      ownerTeam: 'Team A',
      lifecycle: Lifecycle.ACTIVE,
      updatedAt: '2024-01-01T00:00:00Z',
      createdAt: '2024-01-01T00:00:00Z',
      endpoints: [],
      changelog: []
    };

    service.get('1').subscribe(api => {
      expect(api.name).toBe('Test API');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/api/apis/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockApi);
  });
});

