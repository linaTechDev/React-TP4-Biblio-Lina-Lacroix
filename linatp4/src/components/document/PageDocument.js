/*
import React, {useEffect, useState} from 'react';
import HeaderAdd from "../HeaderAdd";
import Documents from "./Documents";
import AddDocument from "./AddDocument";

const PageDocument = () => {

    useEffect(() => {
        const getDocuments = async () => {
            const documentsFromServer = await fetchDocuments()
            setDocuments(documentsFromServer)
        }
        getDocuments()
    }, [])

    const fetchDocuments = async () => {
        const res = await fetch('http://localhost:8080/documents')
        const data = await res.json()
        return data
    }

    const [showAddDocument, setShowAddDocument] = useState(false)
    const [documents, setDocuments] = useState([])

    const addDocument = async (document) => {
        const res = await fetch('http://localhost:8080/documents',
            {
                method: 'POST',
                headers: {
                    'Content-type': 'application/json',
                },
                body: JSON.stringify(document)
            })
        const data = await res.json()
        setDocuments([...documents, data])
    }

    const deleteDocument = async (id) => {
        await fetch(`http://localhost:8080/documents/${id}`, {
            method: 'DELETE'
        })
        setDocuments(documents.filter((document) => document.id !== id))
    }

    return (
        <div className='container'>
            <HeaderAdd title='Document'
                     onAdd={() =>
                         setShowAddDocument(!showAddDocument)}
                     showAdd={showAddDocument}/>
            {showAddDocument && <AddDocument onAdd={addDocument} />}
            {documents.length > 0 ?
                <Documents documents={documents}
                           onDelete={deleteDocument} />
                : 'No Documents'}
        </div>
    );
}

export default PageDocument*/
