import { useState, useEffect } from 'react';
import GroupStage from '../components/GroupStage';
import SubNavbar from '../components/SubNavbar';
import Bracket from '../components/Bracket';
import { getSampleWorldCup } from '../api';

export default function WorldCupPage({ tournament }) {
  const [data, setData] = useState(tournament || null);
  const [loading, setLoading] = useState(!tournament);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!tournament) {
      getSampleWorldCup()
        .then(setData)
        .catch(setError)
        .finally(() => setLoading(false));
    }
  }, [tournament]);

  if (loading) return <div style={{paddingTop: 180, textAlign: 'center'}}>Loading...</div>;
  if (error) return <div style={{paddingTop: 180, textAlign: 'center', color: 'red'}}>Error: {error.message}</div>;
  if (!data) return null;

  const navItems = [
    ...data.groups.map((g) => ({ name: `Group ${g.name}`, link: `#group-${g.name}` })),
    { name: 'Knockout Stage', link: '#knockout' },
    { name: 'Final', link: '#final' },
  ];

  return (
    <div>
      <div style={{paddingTop: 180, maxWidth: 1200, margin: '0 auto', paddingLeft:24, paddingRight:24}}>
        <h2>{data.name} — {data.year}</h2>
        <p>Wiki-style tournament view: group stage, matches, and knockout bracket.</p>
        <SubNavbar items={navItems} />
        <h3 id="group-stage">Group Stage</h3>
        <GroupStage groups={data.groups} />
        <h3 id="knockout" style={{ marginTop: 24 }}>
          Knockout Stage
        </h3>
        <Bracket data={data.knockout} />
      </div>
    </div>
  );
}
