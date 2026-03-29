import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideAnimations } from '@angular/platform-browser/animations'; 

// 1. Importa la configurazione di PrimeNG e il tema Aura
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeng/themes/aura';
// Importa queste utilità da @angular/common/http
import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

// Importa la tua classe (attento al percorso corretto!)


import { routes } from './app.routes';
import { AuthInterceptor } from './interceptors/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideAnimations(),
    // 2. Attiva PrimeNG con il tema Aura
    providePrimeNG({
        theme: {
            preset: Aura,
            options: {
                darkModeSelector: false || 'none' // Forza il tema chiaro per ora
            }
        }
    }),
    provideHttpClient(withInterceptorsFromDi()),
    
    // 2. Registriamo il tuo AuthInterceptor specifico
    { 
      provide: HTTP_INTERCEPTORS, 
      useClass: AuthInterceptor, 
      multi: true 
    }
  ]
};
