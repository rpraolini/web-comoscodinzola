import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class FotoService {
  private apiUrlFoto = environment.apiUrl + '/private/foto';
  private apiUrlVideo = environment.apiUrl + '/private/video';

  constructor(private http: HttpClient) {}

  getFoto(idAnimale: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrlFoto}/${idAnimale}`);
  }

uploadFoto(idAnimale: string, file: File, didascalia: string,
           idTipoFoto: string, pubblica: string): Observable<any> {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('didascalia', didascalia || '');
  formData.append('idTipoFoto', idTipoFoto || '0');
  formData.append('pubblica', pubblica || '0');  // ← nuovo
  return this.http.post(`${this.apiUrlFoto}/${idAnimale}/upload`, formData);
}

togglePubblica(idFoto: string, pubblica: string): Observable<any> {
  return this.http.put(`${this.apiUrlFoto}/${idFoto}/pubblica`, { pubblica });
}

  deleteFoto(idFoto: string): Observable<any> {
    return this.http.delete(`${this.apiUrlFoto}/${idFoto}`);
  }

  impostaProfilo(idFoto: string): Observable<any> {
    return this.http.put(`${this.apiUrlFoto}/${idFoto}/profilo`, {});
  }

  getVideo(idAnimale: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrlVideo}/${idAnimale}`);
  }

  saveVideo(video: any): Observable<any> {
    return this.http.post(`${this.apiUrlVideo}`, video);
  }

  deleteVideo(idVideo: string): Observable<any> {
    return this.http.delete(`${this.apiUrlVideo}/${idVideo}`);
  }
}