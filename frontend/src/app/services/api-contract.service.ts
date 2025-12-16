import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import {
  ApiSummary,
  ApiDetail,
  ApiCreateDto,
  ApiUpdateDto,
  Endpoint,
  EndpointCreateDto,
  ChangelogEntry,
  ChangelogCreateDto,
  ApiListParams,
  Lifecycle,
  SyncMode,
  Diff,
  ImportResult,
  ApiSyncRun
} from '../models/api-contract.model';

@Injectable({
  providedIn: 'root'
})
export class ApiContractService {
  // In Docker, nginx proxies /api to backend, so use relative path
  // In local dev, use full URL
  private baseUrl = (environment.apiUrl.startsWith('http') ? `${environment.apiUrl}/api/apis` : '/api/apis');

  constructor(private http: HttpClient) {}

  list(params?: ApiListParams): Observable<ApiSummary[]> {
    let httpParams = new HttpParams();
    
    if (params?.q) {
      httpParams = httpParams.set('q', params.q);
    }
    if (params?.lifecycle) {
      httpParams = httpParams.set('lifecycle', params.lifecycle);
    }
    if (params?.ownerTeam) {
      httpParams = httpParams.set('ownerTeam', params.ownerTeam);
    }
    if (params?.sort) {
      httpParams = httpParams.set('sort', params.sort);
    }
    if (params?.dir) {
      httpParams = httpParams.set('dir', params.dir);
    }

    return this.http.get<ApiSummary[]>(this.baseUrl, { params: httpParams });
  }

  get(id: string): Observable<ApiDetail> {
    return this.http.get<ApiDetail>(`${this.baseUrl}/${id}`);
  }

  create(dto: ApiCreateDto): Observable<ApiDetail> {
    console.log('Creating API:', dto);
    console.log('POST to:', this.baseUrl);
    return this.http.post<ApiDetail>(this.baseUrl, dto);
  }

  update(id: string, dto: ApiUpdateDto): Observable<ApiDetail> {
    return this.http.put<ApiDetail>(`${this.baseUrl}/${id}`, dto);
  }

  patchLifecycle(id: string, lifecycle: Lifecycle): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${id}/lifecycle`, { lifecycle });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  addEndpoint(apiId: string, dto: EndpointCreateDto): Observable<Endpoint> {
    return this.http.post<Endpoint>(`${this.baseUrl}/${apiId}/endpoints`, dto);
  }

  updateEndpoint(apiId: string, endpointId: string, dto: EndpointCreateDto): Observable<Endpoint> {
    return this.http.put<Endpoint>(`${this.baseUrl}/${apiId}/endpoints/${endpointId}`, dto);
  }

  deleteEndpoint(apiId: string, endpointId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${apiId}/endpoints/${endpointId}`);
  }

  addChangelogEntry(apiId: string, dto: ChangelogCreateDto): Observable<ChangelogEntry> {
    return this.http.post<ChangelogEntry>(`${this.baseUrl}/${apiId}/changelog`, dto);
  }

  deleteChangelogEntry(apiId: string, entryId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${apiId}/changelog/${entryId}`);
  }

  previewOpenApiDiff(apiId: string): Observable<Diff> {
    return this.http.get<Diff>(`${this.baseUrl}/${apiId}/openapi-diff`);
  }

  importOpenApi(apiId: string, mode: SyncMode): Observable<ImportResult> {
    return this.http.post<ImportResult>(`${this.baseUrl}/${apiId}/import-openapi`, { mode });
  }

  getSyncRuns(apiId: string): Observable<ApiSyncRun[]> {
    return this.http.get<ApiSyncRun[]>(`${this.baseUrl}/${apiId}/sync-runs`);
  }
}

