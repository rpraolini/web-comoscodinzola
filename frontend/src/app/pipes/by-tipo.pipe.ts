import { Pipe, PipeTransform } from "@angular/core";

// by-tipo.pipe.ts
@Pipe({ name: 'byTipo', standalone: true })
export class ByTipoPipe implements PipeTransform {
  transform(caratteri: any[], idTipo: string): any[] {
    return caratteri.filter(c => c.id_tipo_carattere === idTipo);
  }
}