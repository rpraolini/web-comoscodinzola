import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ContentRoutingModule } from './content-routing.module';
import { provideHttpClient, withInterceptorsFromDi, withJsonpSupport } from '@angular/common/http';

import { ContentComponent } from './content.component';
import { ContentService } from './content.service';
import { RicercaModule } from './ricerca/ricerca.module';
import { LietiComponent } from './lieti/lieti.component';
import { SchedaComponent } from './scheda/scheda.component';
//import { YoutubePlayerModule } from 'ngx-youtube-player';
//import { ShareButtonModule } from '@ngx-share/button';


@NgModule({ declarations: [ContentComponent, LietiComponent, SchedaComponent], imports: [CommonModule,
        RicercaModule,
        //YoutubePlayerModule,
        //ShareButtonModule,
        ContentRoutingModule], providers: [ContentService, provideHttpClient(withInterceptorsFromDi(), withJsonpSupport())] })
export class ContentModule { }
