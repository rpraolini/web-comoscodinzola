import { Component, OnInit } from '@angular/core';
import { ContentService } from '../content.service';
import { Router } from '@angular/router';


@Component({
    selector: 'app-ricerca',
    templateUrl: './ricerca.component.html',
    standalone: false
})
export class RicercaComponent implements OnInit {

  animali: Array<any>;
  regioni: Array<any>;
  province: Array<any>;
  model: any = {};
  strForList: String;
  strCount: String;

  constructor(private service: ContentService, private router: Router) {
    //this.reset();
  }

  ngOnInit() {
    if(localStorage.getItem("ricercaPubblica")){
      this.animali = JSON.parse(localStorage.getItem("ricercaPubblica"));
      this.model = JSON.parse(localStorage.getItem("model"));
    }else{
      this.getAnimali();
    }
     this.getRegioni(this.model.nazione);
  }

  ricerca(): void {
    this.strForList = 'Alcuni dei nostri amici che hai ricercato :';
    this.animali = [];
    this.service.ricerca(this.model).subscribe(data => {
      this.animali = data;
      localStorage.setItem("ricercaPubblica", JSON.stringify(data))
      localStorage.setItem("model", JSON.stringify(this.model))
      this.strCount = 'Trovate ' + this.animali.length + ' corrispondenze.';
    });
  }

  getAnimali(): void {
    this.strForList = 'Ecco alcuni dei nostri amici :';
    this.strCount = '';
    this.service.getAll().subscribe(data => {
      this.animali = data;
    });
  }

  getRegioni(nazione: String): void {
    this.regioni =  [];
    this.province = [];
    this.model.regione = '';
    this.model.provincia = '';
    this.service.getRegioni(nazione).subscribe(data => {
      this.regioni = data;
    });
  }

  getProvince(regione: any): void {
    this.service.getProvince(regione).subscribe(data => {
      this.province = data;
    });
  }

  reset(): void {
    this.model = {'tipo': '', 'sesso': '', 'taglia': '', 'eta': '', 'nazione': 'IT', 'regione': '', 'provincia': ''};
    this.getAnimali();
  }

  onSelect(animale): void {
    this.router.navigate(['/scheda', animale.id_animale]);
  }

}
