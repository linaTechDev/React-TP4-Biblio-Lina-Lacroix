import {useEffect, useState} from "react";
import Header2 from "../../Header2";
import AddCd from "./AddCd";
import Cds from "./Cds";

const PageCd = () => {
    useEffect(() => {
        const getCds = async () => {
            const cdsFromServer = await fetchCds()
            setCds(cdsFromServer)
        }
        getCds()
    }, [])

    const fetchCds = async () => {
        const res = await fetch('http://localhost:8080/documents/cds')
        const data = await res.json()
        return data
    }

    const [showAddCd, setShowAddCd] = useState(false)
    const [cds, setCds] = useState([])

    const addCd = async (cd) => {
        const res = await fetch('http://localhost:8080/documents/cds',
            {
                method: 'POST',
                headers: {
                    'Content-type': 'application/json',
                },
                body: JSON.stringify(cd)
            })
        const data = await res.json()
        setCds([...cds, data])
    }

    const deleteCd = async (id) => {
        await fetch(`http://localhost:8080/documents/cds/${id}`, {
            method: 'DELETE'
        })
        setCds(cds.filter((cd) => cd.id !== id))
    }

    return (
        <div className='container'>
            <Header2 title='Document cd'
                     onAdd={() =>
                     setShowAddCd(!showAddCd)}
                     showAdd={showAddCd}/>
            {showAddCd && <AddCd onAdd={addCd} />}
            {cds.length > 0 ?
            <Cds cds={cds}
                onDelete={deleteCd}/>
            : 'No Documents cds'}
        </div>
    );
}

export default PageCd