import GroupStage from '../components/GroupStage';
import SubNavbar from '../components/SubNavbar';
import GlobalNavbar from '../components/GlobalNavbar';

const sample = {
  name: 'Fictional World Cup',
  year: 2026,
  groups: [
    { name: 'A', teams: [ {id:'qatar',name:'Qatar'},{id:'ecu',name:'Ecuador'},{id:'sen',name:'Senegal'},{id:'ned',name:'Netherlands'} ], matches: [
      { day:1, date:'15 Jun', group:'A', home:{id:'ned',name:'Netherlands'}, away:{id:'ecu',name:'Ecuador'}, homeScore:3, awayScore:0, venue:'Stadium A', time:'20:00', scorers:[ {team:'home',player:'G. Wijnaldum',minute:23}, {team:'home',player:'D. Bergwijn',minute:67}, {team:'home',player:'B. Devrij',minute:81} ] },
      { day:1, date:'15 Jun', group:'A', home:{id:'qatar',name:'Qatar'}, away:{id:'sen',name:'Senegal'}, homeScore:1, awayScore:2, venue:'Stadium B', time:'17:00', scorers:[ {team:'away',player:'S. Mane',minute:12}, {team:'home',player:'A. Almoez',minute:55}, {team:'away',player:'I. Gueye',minute:88} ] }
    ] },
    { name: 'B', teams: [ {id:'eng',name:'England'},{id:'iran',name:'Iran'},{id:'usa',name:'USA'},{id:'wales',name:'Wales'} ] },
    { name: 'C', teams: [ {id:'arg',name:'Argentina'},{id:'saudi',name:'Saudi Arabia'},{id:'mex',name:'Mexico'},{id:'pol',name:'Poland'} ] },
    { name: 'D', teams: [ {id:'fra',name:'France'},{id:'aus',name:'Australia'},{id:'den',name:'Denmark'},{id:'tun',name:'Tunisia'} ] },
    { name: 'E', teams: [ {id:'esp',name:'Spain'},{id:'crc',name:'Costa Rica'},{id:'ger',name:'Germany'},{id:'jpn',name:'Japan'} ] },
    { name: 'F', teams: [ {id:'bel',name:'Belgium'},{id:'can',name:'Canada'},{id:'mor',name:'Morocco'},{id:'cro',name:'Croatia'} ] },
    { name: 'G', teams: [ {id:'bra',name:'Brazil'},{id:'srb',name:'Serbia'},{id:'sui',name:'Switzerland'},{id:'cmr',name:'Cameroon'} ] },
    { name: 'H', teams: [ {id:'por',name:'Portugal'},{id:'gha',name:'Ghana'},{id:'uru',name:'Uruguay'},{id:'kor',name:'South Korea'} ] }
  ],
    matchdays: [
      {day:1,date:'15 Jun', matches: [
        { group:'A', home:{id:'ned',name:'Netherlands'}, away:{id:'ecu',name:'Ecuador'}, homeScore:3, awayScore:0, venue:'Stadium A', time:'20:00', scorers:[ {team:'home',player:'G. Wijnaldum',minute:23}, {team:'home',player:'D. Bergwijn',minute:67}, {team:'home',player:'B. Devrij',minute:81} ] },
        { group:'A', home:{id:'qatar',name:'Qatar'}, away:{id:'sen',name:'Senegal'}, homeScore:1, awayScore:2, venue:'Stadium B', time:'17:00', scorers:[ {team:'away',player:'S. Mane',minute:12}, {team:'home',player:'A. Almoez',minute:55}, {team:'away',player:'I. Gueye',minute:88} ] }
      ] }
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

export default function WorldCupPage({ tournament }) {
  const t = tournament || sample;

  const navItems = [
    ...t.groups.map((g) => ({ name: `Group ${g.name}`, link: `#group-${g.name}` })),
    { name: 'Knockout Stage', link: '#knockout' },
    { name: 'Final', link: '#final' },
  ];

  return (
    <div>
      <GlobalNavbar />
      <div style={{paddingTop: 140, maxWidth: 1200, margin: '0 auto', paddingLeft:24, paddingRight:24}}>
        <h2>{t.name} — {t.year}</h2>
        <p>Wiki-style tournament view: group stage, matches, and knockout bracket.</p>
        <SubNavbar items={navItems} />
        <h3 id="group-stage">Group Stage</h3>
        <GroupStage groups={t.groups} />
        <h3 id="knockout" style={{ marginTop: 24 }}>
          Knockout Stage
        </h3>
      </div>
    </div>
  );
}
