import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class SaluteService {
  private apiUrl = environment.apiUrl + '/private/salute';
  private apiUrlDoc = environment.apiUrl + '/private/documenti';

  constructor(private http: HttpClient) { }

  getEventi(idAnimale: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${idAnimale}`);
  }

  getTipi(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/tipi`);
  }

  getCategorie(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/categorie`);
  }

  save(evento: any): Observable<any> {
    return this.http.post(this.apiUrl, evento);
  }

  update(idEvento: string, evento: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${idEvento}`, evento);
  }

  delete(idEvento: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${idEvento}`);
  }

  // Documenti — riusa l'infrastruttura esistente
  uploadDocumento(idEvento: string, idAnimale: string, tipoDoc: any, file: File): Observable<any> {
    const documento = { id_tipo_documento: tipoDoc.id_tipo_documento };
    const formData = new FormData();
    formData.append('idAnimale', idAnimale);
    formData.append('documento', JSON.stringify(documento));
    formData.append('file', file);
    return this.http.post(`${this.apiUrl}/${idEvento}/documento`, formData);
  }



  deleteDocumento(idDocumento: string): Observable<any> {
    return this.http.delete(`${this.apiUrlDoc}/deleteDocumento`, { params: { id: idDocumento } });
  }

  getTipiDocumento(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrlDoc}/E`);
  }



}