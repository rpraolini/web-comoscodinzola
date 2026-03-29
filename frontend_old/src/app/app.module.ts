import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { LocationStrategy, HashLocationStrategy} from '@angular/common';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';


import { AppComponent } from './app.component';

import { HeaderComponent } from './header/header.component';
import { ContentModule } from './content/content.module';
import { AppRoutingModule } from './app-routing.module';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

@NgModule({ declarations: [AppComponent, HeaderComponent],
    bootstrap: [AppComponent], imports: [BrowserModule,
        FormsModule,
        ContentModule,
        AppRoutingModule,
        FontAwesomeModule], providers: [{ provide: LocationStrategy, useClass: HashLocationStrategy }, provideHttpClient(withInterceptorsFromDi())] })
export class AppModule { }
