import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AdozioneService {
  private apiUrl = environment.apiUrl + '/private/adozione';

  constructor(private http: HttpClient) {}

  getPratiche(idAnimale: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${idAnimale}`);
  }

  nuovaPratica(idAnimale: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/pratica/${idAnimale}`, {});
  }

  chiudiPratica(idPratica: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/pratica/${idPratica}/chiudi`, {});
  }

  eliminaPratica(idPratica: string, idAnimale: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/pratica/${idPratica}`, { params: { idAnimale } });
  }

  getTipi(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/tipi`);
  }

  aggiungiIter(iter: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/iter`, iter);
  }

  aggiornaIter(idIter: string, iter: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/iter/${idIter}`, iter);
  }

  eliminaIter(idIter: string, idAnimale: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/iter/${idIter}`, { params: { idAnimale } });
  }

  exportPassaggioPdf(idIter: string, idAnimale: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/iter/${idIter}/passaggio/pdf`, {
      params: { idAnimale },
      responseType: 'blob'
    });
  }

  exportAffidoPdf(idIter: string, idAnimale: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/iter/${idIter}/affido/pdf`, {
      params: { idAnimale },
      responseType: 'blob'
    });
  }

  exportQuestionarioPdf(idIter: string, questKey: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/iter/${idIter}/questionario/pdf`, {
      params: { questKey },
      responseType: 'blob'
    });
  }

  chiudiIstruttoria(idAnimale: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/istruttoria/${idAnimale}/chiudi`, {});
  }

  saveDocumentoIter(idIter: string, idAnimale: string, tipoDoc: any, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('idAnimale', idAnimale);
    formData.append('documento', JSON.stringify({
      id_tipo_documento: tipoDoc.id_tipo_documento,
      documento: tipoDoc.documento
    }));
    formData.append('file', file);
    return this.http.post(`${this.apiUrl}/iter/${idIter}/documento`, formData);
  }
}