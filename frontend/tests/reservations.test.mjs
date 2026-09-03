import {test} from 'node:test';
import assert from 'node:assert/strict';
import {validate,overlaps,time} from '../lib/reservations.ts';
const now=new Date('2026-09-01T12:00:00Z');
const draft={room:'norte',date:'2026-09-02',start:600,duration:60,title:' Planeación ',name:' Equipo '};
test('normaliza texto y conserva un horario válido',()=>assert.equal(validate(draft,now).title,'Planeación'));
test('límites contiguos no se superponen',()=>{assert.equal(overlaps({start:600,duration:60},{start:660,duration:30}),false);assert.equal(overlaps({start:600,duration:60},{start:630,duration:30}),true)});
test('rechaza horas, fechas, duración y datos incorrectos',()=>{for(const v of [{start:541},{start:1080},{duration:45},{date:'2027-02-30'},{date:'2026-08-01'},{room:'x'},{title:' '},{name:''},{date:'2027-09-01'}])assert.throws(()=>validate({...draft,...v},now))});
test('formatea bloques de media hora',()=>assert.equal(time(570),'09:30'));
