import {env} from 'cloudflare:workers';
import {validate} from '@/lib/reservations';
const json=(body:unknown,status=200)=>Response.json(body,{status,headers:{'Cache-Control':'no-store'}});
async function handle(request:Request){const u=new URL(request.url);const path=u.pathname.replace('/api/','');const e=env as unknown as {DB:D1Database;TURNO_API_BASE?:string};
if(request.method!=='GET'&&request.headers.get('origin')!==u.origin)return json({message:'Origen no permitido.'},403);
if(!/^(bookings|bookings\/[0-9a-f-]{36}\/cancel|health)$/.test(path))return json({message:'Ruta no encontrada.'},404);
if(e.TURNO_API_BASE){try {const r=await fetch(e.TURNO_API_BASE+'/api/'+path+u.search,{method:request.method,headers:{'Content-Type':'application/json'},body:request.method==='GET'?undefined:await request.text(),redirect:'error',signal:AbortSignal.timeout(10000)});return new Response(await r.text(),{status:r.status,headers:{'Content-Type':'application/json','Cache-Control':'no-store'}})}catch{return json({message:'No se pudo conectar con el servidor. Intenta de nuevo.'},503)}}
const db=e.DB;
try{
if(path==='health'&&request.method==='GET'){await db.prepare('SELECT 1').first();return json({status:'UP',mode:'hosted'})}
if(path==='bookings'&&request.method==='GET'){const date=u.searchParams.get('date');if(!date||!/^\d{4}-\d{2}-\d{2}$/.test(date))return json({message:'Indica una fecha válida.'},400);const rows=await db.prepare('SELECT id,room,date,start,duration,title,name,status FROM bookings WHERE date=? ORDER BY start,id LIMIT 500').bind(date).all();return json(rows.results)}
if(path==='bookings'&&request.method==='POST'){let v;try{const raw=await request.text();if(raw.length>4096)throw Error('Solicitud demasiado grande.');v=validate(JSON.parse(raw))}catch(err){return json({message:err instanceof Error?err.message:'Datos inválidos.'},400)}const id=crypto.randomUUID();const statements=[db.prepare('INSERT INTO bookings(id,room,date,start,duration,title,name,status) VALUES(?,?,?,?,?,?,?,?)').bind(id,v.room,v.date,v.start,v.duration,v.title,v.name,'CONFIRMED')];for(let s=v.start;s<v.start+v.duration;s+=30)statements.push(db.prepare('INSERT INTO slots(id,booking_id,room,date,start) VALUES(?,?,?,?,?)').bind(crypto.randomUUID(),id,v.room,v.date,s));try{await db.batch(statements)}catch(err){if(String(err).includes('UNIQUE constraint failed'))return json({message:'Ese horario acaba de ocuparse. Elige otro.'},409);throw err}return json({id,...v,status:'CONFIRMED'},201)}
if(path.endsWith('/cancel')&&request.method==='POST'){const id=path.split('/')[1];const found=await db.prepare('SELECT id FROM bookings WHERE id=?').bind(id).first();if(!found)return json({message:'Reserva no encontrada.'},404);await db.batch([db.prepare("UPDATE bookings SET status='CANCELLED' WHERE id=?").bind(id),db.prepare('DELETE FROM slots WHERE booking_id=?').bind(id)]);return json({status:'CANCELLED'})}
return json({message:'Método no permitido.'},405);
}catch(err){console.error('Reservation operation failed',err);return json({message:'No fue posible guardar los cambios. Intenta de nuevo.'},500)}}
export const GET=handle;export const POST=handle;
