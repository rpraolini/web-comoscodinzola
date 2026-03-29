import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';


@Injectable({
  providedIn: 'root'
})
export class ContentService {

  constructor(private http: HttpClient) { }

  env = environment;

  getConf(): Observable<any> {
    return this.http.get(this.env.urlHost + '/jsp/private/configurazione/getAll.json');
  }

  getAll(): Observable<any> {
    return this.http.get(this.env.urlHost + '/jsp/public/getRandom.json');
  }

  getLietiFine(arg: any): Observable<any> {
    return this.http.get(this.env.urlHost + '/jsp/public/getLietiFine.json', {params: {anno: arg}});
  }

  getLietiFineCount(): Observable<any> {
    return this.http.get(this.env.urlHost + '/jsp/public/getLietiFineCount.json');
  }

  getLietiFineCountByAnno(): Observable<any> {
    return this.http.get(this.env.urlHost + '/jsp/public/getLietiFineCountByAnno.json');
  }

  getRegioni(arg): Observable<any> {
    return this.http.get(this.env.urlHost + '/jsp/public/getRegioniByNazione.json', {params: {nazione: arg}});
  }

  getProvince(arg): Observable<any> {
    return this.http.get(this.env.urlHost + '/jsp/public/getProvince.json', {params: {id: arg}});
  }

  ricerca(arg): Observable<any> {
    return this.http.get(this.env.urlHost + '/jsp/public/ricerca.json', {params: arg});
  }

  getById(arg): Observable<any> {
    return this.http.get(this.env.urlHost + '/jsp/public/getById.json', {params: {id: arg}});
  }

  getCaratteriById(arg): Observable<any> {
    return this.http.get(this.env.urlHost + '/jsp/public/getCaratteriById.json', {params: {id: arg}});
  }

  getVideoById(arg): Observable<any> {
    return this.http.get(this.env.urlHost + '/jsp/public/getVideoById.json', {params: {id: arg}});
  }

  getFotoById(arg): Observable<any> {
    return this.http.get(this.env.urlHost + '/jsp/public/getFotoById.json', {params: {id: arg}});
  }

}

