export const ROOMS=[{id:'norte',name:'Sala Norte',size:6},{id:'sur',name:'Sala Sur',size:10}];
export type Booking={id:string;room:string;date:string;start:number;duration:number;title:string;name:string;status:'CONFIRMED'|'CANCELLED'};
export type Draft=Omit<Booking,'id'|'status'>;
export const today=()=>new Intl.DateTimeFormat('en-CA',{timeZone:'America/Mexico_City',year:'numeric',month:'2-digit',day:'2-digit'}).format(new Date());
export const shiftDate=(date:string,days:number)=>{const d=new Date(date+'T12:00:00Z');d.setUTCDate(d.getUTCDate()+days);return d.toISOString().slice(0,10)};
export const time=(n:number)=>`${String(Math.floor(n/60)).padStart(2,'0')}:${String(n%60).padStart(2,'0')}`;
export function validate(v:unknown,now=new Date()):Draft{if(!v||typeof v!=='object')throw Error('Revisa los datos de la reserva.');const x=v as Record<string,unknown>;const {room,date,start,duration,title,name}=x;
if(!ROOMS.some(r=>r.id===room)||typeof date!=='string'||!/^\d{4}-\d{2}-\d{2}$/.test(date)||Number.isNaN(Date.parse(date+'T12:00:00Z'))||new Date(date+'T12:00:00Z').toISOString().slice(0,10)!==date)throw Error('Sala o fecha inválida.');
if(typeof start!=='number'||!Number.isInteger(start)||start%30!==0||start<540||typeof duration!=='number'||![30,60,90].includes(duration)||start+duration>1080)throw Error('Elige un horario entre las 09:00 y las 18:00.');
const first=new Date(`${date}T${time(start)}:00-06:00`);if(first<=now||first.getTime()>now.getTime()+90*86400000)throw Error('Reserva un horario futuro dentro de los próximos 90 días.');
if(typeof title!=='string'||title.trim().length<3||title.trim().length>80||typeof name!=='string'||name.trim().length<2||name.trim().length>60)throw Error('Escribe un motivo de 3 a 80 caracteres y un nombre de 2 a 60.');
return {room:room as string,date,start,duration,title:title.trim(),name:name.trim()};}
export const overlaps=(a:Pick<Booking,'start'|'duration'>,b:Pick<Booking,'start'|'duration'>)=>a.start<b.start+b.duration&&b.start<a.start+a.duration;
