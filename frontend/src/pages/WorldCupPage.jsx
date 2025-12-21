import GroupStage from '../components/GroupStage'

const sample = {
  name: 'Fictional World Cup',
  year: 2026,
  groups: [
    { name: 'A', teams: [ {id:'qatar',name:'Qatar'},{id:'ecu',name:'Ecuador'},{id:'sen',name:'Senegal'},{id:'ned',name:'Netherlands'} ] },
    { name: 'B', teams: [ {id:'eng',name:'England'},{id:'iran',name:'Iran'},{id:'usa',name:'USA'},{id:'wales',name:'Wales'} ] },
    { name: 'C', teams: [ {id:'arg',name:'Argentina'},{id:'saudi',name:'Saudi Arabia'},{id:'mex',name:'Mexico'},{id:'pol',name:'Poland'} ] },
    { name: 'D', teams: [ {id:'fra',name:'France'},{id:'aus',name:'Australia'},{id:'den',name:'Denmark'},{id:'tun',name:'Tunisia'} ] },
    { name: 'E', teams: [ {id:'esp',name:'Spain'},{id:'crc',name:'Costa Rica'},{id:'ger',name:'Germany'},{id:'jpn',name:'Japan'} ] },
    { name: 'F', teams: [ {id:'bel',name:'Belgium'},{id:'can',name:'Canada'},{id:'mor',name:'Morocco'},{id:'cro',name:'Croatia'} ] },
    { name: 'G', teams: [ {id:'bra',name:'Brazil'},{id:'srb',name:'Serbia'},{id:'sui',name:'Switzerland'},{id:'cmr',name:'Cameroon'} ] },
    { name: 'H', teams: [ {id:'por',name:'Portugal'},{id:'gha',name:'Ghana'},{id:'uru',name:'Uruguay'},{id:'kor',name:'South Korea'} ] }
  ],
  // seeded knockout (round of 16) using typical group pairing logic
  knockout: {
    roundOf16: [
      // A1 vs B2, C1 vs D2, E1 vs F2, G1 vs H2
      ['ned','iran'], ['arg','den'], ['esp','can'], ['bra','gha'],
      // B1 vs A2, D1 vs C2, F1 vs E2, H1 vs G2
      ['eng','ecu'], ['fra','saudi'], ['bel','crc'], ['por','srb']
    ],
    quarterfinals: [],
    semifinals: [],
    final: [],
    thirdPlace: []
  }
}

export default function WorldCupPage({ tournament }){
  const t = tournament || sample
  return (
    <div>
      <h2>{t.name} — {t.year}</h2>
      <p>Wiki-style tournament view: group stage, matches, and knockout bracket.</p>
      <h3>Group Stage</h3>
      <GroupStage groups={t.groups} />

      <h3 style={{marginTop:24}}>Knockout Stage</h3>

    </div>
  )
}
