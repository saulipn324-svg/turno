import assert from 'node:assert/strict';
const base=process.argv[2]||'http://localhost:3004';
const date=new Date(Date.now()+30*86400000).toISOString().slice(0,10);
const created=[];
async function request(path,body){const r=await fetch(base+'/api/'+path,{method:body===undefined?'GET':'POST',headers:{'Content-Type':'application/json',Origin:base},body:body===undefined?undefined:JSON.stringify(body)});return {status:r.status,data:await r.json()}}
const draft={room:'norte',date,start:600,duration:90,title:'Prueba automatizada',name:'Equipo QA'};
try{
 const existing=await request('bookings?date='+date);assert.equal(existing.status,200);
 // Refuse to modify existing reservations belonging to another run.
 assert.equal(existing.data.filter(b=>b.status==='CONFIRMED').length,0,'El día de prueba tiene reservas: usa otra fecha antes de ejecutar.');
 const results=await Promise.all([request('bookings',draft),request('bookings',draft)]);
 for(const r of results)if(r.status===201)created.push(r.data.id);
 assert.deepEqual(results.map(r=>r.status).sort(),[201,409]);
 const conflict=await request('bookings',{...draft,start:570});assert.equal(conflict.status,409);
 let list=await request('bookings?date='+date);assert.equal(list.data.filter(b=>b.status==='CONFIRMED').length,1);
 for(const d of [{...draft,start:690,duration:30},{...draft,room:'sur'}]){const r=await request('bookings',d);assert.equal(r.status,201);created.push(r.data.id)}
 assert.equal((await request('bookings',{...draft,start:541})).status,400);
 assert.equal((await request('bookings',{...draft,date:'2027-02-30'})).status,400);
 assert.equal((await request('bookings',{...draft,date:'2020-01-01'})).status,400);
 assert.equal((await request('bookings',{...draft,duration:45})).status,400);
 assert.equal((await request('bookings',{...draft,title:''})).status,400);
 assert.equal((await request('bookings/'+created[0]+'/cancel',{})).status,200);
 assert.equal((await request('bookings/'+created[0]+'/cancel',{})).status,200);
 const replacement=await request('bookings',draft);assert.equal(replacement.status,201);created.push(replacement.data.id);
 console.log('APROBADO: concurrencia, solapamiento, rollback, salas independientes, adyacencia, validación y cancelación idempotente.');
}finally{for(const id of created)await request('bookings/'+id+'/cancel',{})}
