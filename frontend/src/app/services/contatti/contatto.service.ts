import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ContattoService {
  private apiUrl = environment.apiUrl + '/private/contatti';

  constructor(private http: HttpClient) {}

  autocomplete(query: string, idTipoEvento: string = ''): Observable<any[]> {
  return this.http.get<any[]>(`${this.apiUrl}/autocomplete`, {
    params: { query, idTipoEvento }
  });
}

}