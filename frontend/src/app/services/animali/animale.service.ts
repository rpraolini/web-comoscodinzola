import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';


@Injectable({
  providedIn: 'root'
})
export class AnimaleService {

  private apiUrl = environment.apiUrl + '/private/animali'; 
  private apiUrlTags = environment.apiUrl + '/private/tags'; 
  private apiUrlProcesso = environment.apiUrl + '/private/processo'; 

  constructor(private http: HttpClient) { }

  // Metodo per recuperare tutta la lista degli ultimi animali inseriti (50)
  getAnimali(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  cercaCompleta(filtri: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/ricerca-completa`, filtri);
  }

  getStati(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/stati`);
  }
  getRegioni(nazione: string = 'IT'): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/regioni/${nazione}`);
  }

  getProvince(idRegione: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/province/${idRegione}`);
  }

  // Recupera la lista dei colori
  getColori(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/getColori.json`);
  }

  // Recupera la lista delle razze
  getRazze(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/getRazze.json`);
  }

  saveAnimale(animale: any): Observable<any> {
    // Il backend si aspetta una POST verso l'endpoint di salvataggio
    return this.http.post<any>(`${this.apiUrl}/save`, animale);
  }
  getById(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  registraDecesso(idAnimale: string, data: Date): Observable<any> {
    // Trasmettiamo i dati al controller Java
    return this.http.post(`${this.apiUrlProcesso}/registraDecesso`, {
        id_animale: idAnimale,
        dt_decesso: data
    });
  }

  eliminaAnimale(id: string): Observable<any> {
  return this.http.delete<any>(`${this.apiUrl}/${id}`);
}

  /////////////////////////// TAGS //////////////////////////

  getTags(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrlTags);
  }

  getTagsByAnimale(id: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrlTags}/${id}`);
  }

  assignTag(idAnimale: string, idTag: string): Observable<any> {
    return this.http.post(`${this.apiUrlTags}/${idAnimale}/tags/${idTag}`, {});
  }

  removeTag(idAnimale: string, idTag: string): Observable<any> {
    return this.http.delete(`${this.apiUrlTags}/${idAnimale}/tags/${idTag}`);
  }

  updateAnimale(id: string, animale: any): Observable<any> {
  return this.http.put<any>(`${this.apiUrl}/${id}`, animale);
}

validaAnimale(id: string): Observable<any> {
  return this.http.post(`${this.apiUrl}/${id}/valida`, {});
}
  
rendiAdottabile(id: string): Observable<any> {
  return this.http.post(`${this.apiUrl}/${id}/adottabile`, {});
}

revocaAdottabile(id: string): Observable<any> {
  return this.http.post(`${this.apiUrl}/${id}/revoca-adottabile`, {});
}

}