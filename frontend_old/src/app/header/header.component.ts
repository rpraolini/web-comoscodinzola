import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ContentService } from '../content/content.service';

@Component({
    selector: 'app-header',
    templateUrl: './header.component.html',
    standalone: false
})
export class HeaderComponent implements OnInit {
  tenant = "";
  header_1 = "";
  header_2 = "";
  sito = "";

  conf: Array<any>;


  constructor(private router: Router, private service: ContentService,) { }

    routeIsActive(routePath: string) {
        return this.router.url === routePath;
    }

    ngOnInit() { 
      let host = window.location.host;
      this.tenant = host.substring(0,host.indexOf("."));
      let ele = document.getElementById('imgHeader');
      ele.style.backgroundImage = "url('./assets/" + this.tenant + "/header.jpg')";

      this.service.getConf().subscribe(data => {
        this.conf = data;
        this.header_1 = this.conf.find(e => e.chiave === "header_1").descrizione;
        this.header_2 = this.conf.find(e => e.chiave === "header_2").descrizione;
        this.sito = this.conf.find(e => e.chiave === "sito").descrizione;
      });
    }

}

