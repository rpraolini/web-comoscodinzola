import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';


import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.prod';
import { Animale, Carattere, Foto } from '../../models/animale.model';

@Injectable({
  providedIn: 'root'
})
export class AdoptionService {

  private apiUrl = environment.apiUrl + '/public';

  constructor(private http: HttpClient) { }

  // Recupera 9 animali random
  getRandomAnimals(): Observable<Animale[]> {
    // La chiamata sarà: /manorg/api/public/getRandom.json
    return this.http.get<Animale[]>(`${this.apiUrl}/getRandom.json`);
  }

  getAnimalById(id: string): Observable<Animale> {
    return this.http.get<Animale>(`${this.apiUrl}/animale/${id}`); 
  }

  // 1. Recupera i caratteri
  getCaratteri(idAnimale: string): Observable<Carattere[]> {
    const params = new HttpParams().set('id_animale', idAnimale);
    return this.http.get<Carattere[]>(`${this.apiUrl}/getCaratteri.json`, { params });
  }

  // 2. Recupera le foto extra
  getFotoGallery(idAnimale: string): Observable<Foto[]> {
    const params = new HttpParams().set('id_animale', idAnimale);
    return this.http.get<Foto[]>(`${this.apiUrl}/getFoto.json`, { params });
  }

  ricercaAnimali(
      tipo: string, 
      eta: string, 
      taglia: string, 
      sesso: string, 
      regione: string, 
      provincia: string
  ): Observable<Animale[]> {
    
    // Costruiamo i parametri. 
    // Se un valore è null o undefined, inviamo una stringa vuota o un valore di default
    // per evitare errori lato Spring (che si aspetta tutti i parametri).
    
    let params = new HttpParams()
      .set('tipo', tipo || '')
      .set('eta', eta || '')
      .set('taglia', taglia || '')
      .set('sesso', sesso || '')
      .set('regione', regione || '')
      .set('provincia', provincia || '');

    return this.http.get<Animale[]>(`${this.apiUrl}/ricerca.json`, { params });
  }

  // Recupera le regioni (presumo passando 'IT' di default per l'Italia)
  getRegioni(nazione: string = 'IT'): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/regioni/${nazione}`);
  }

  // Recupera le province in base all'ID della regione
  getProvince(idRegione: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/province/${idRegione}`);
  }

  // 1. Recupera il totale assoluto delle adozioni (NOTA: responseType 'text')
  getLietiFineCount(): Observable<string> {
    return this.http.get(`${this.apiUrl}/getLietiFineCount.json`, { responseType: 'text' });
  }

  // 2. Recupera la statistica raggruppata per anno
  getLietiFineCountByAnno(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/getLietiFineCountByAnno.json`);
  }

  // 3. Recupera gli animali adottati in un determinato anno
  getLietiFine(anno: string): Observable<Animale[]> {
    const params = new HttpParams().set('anno', anno);
    return this.http.get<Animale[]>(`${this.apiUrl}/getLietiFine.json`, { params });
  }

}