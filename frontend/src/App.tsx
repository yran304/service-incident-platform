import { type SubmitEvent, useEffect, useState } from "react"
import { 
  createOrganization, 
  getOrganizations, 
  type Organization 
} from "./api/organizations"

function App() {
  const [organizations, setOrganizations] = useState<Organization[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [name, setName] = useState('')
  const [slug, setSlug] = useState('')

  useEffect(() => {
    async function loadOrganizations() {
      try {
        const data = await getOrganizations()
        setOrganizations(data)
      } catch (error) {
        setError("Unable to load organizations")
      } finally {
        setIsLoading(false)
      }
    }

    loadOrganizations()
  }, [])

  async function handleCreateOrganization(
    event: SubmitEvent<HTMLFormElement>,
  ) {
    event.preventDefault()

    try {
      const organization = await createOrganization({ name, slug })

      setOrganizations((currentOrganizations) => [
        ...currentOrganizations,
        organization,
      ])

      setName('')
      setSlug('')
    } catch {
      setError('Unable to create organization')
    }
  }

  if (isLoading) {
    return <p>Loading organizations...</p>
  }

  if (error) {
    return <p>{error}</p>
  }

  return (
    <main>
      <h1>Service Incident Platform</h1>
      <h2>Organizations</h2>

      <form onSubmit={handleCreateOrganization}>
        <label>
          Name
          <input
            value={name}
            onChange={(event) => setName(event.target.value)}
            required
          />
        </label>

        <label>
          Slug
          <input
            value={slug}
            onChange={(event) => setSlug(event.target.value)}
            required
          />
        </label>

        <button type="submit">Create organization</button>
      </form>

      {organizations.length === 0 ? (
        <p>No organizations yet.</p>
      ) : (
        <ul>
          {organizations.map((organization) => (
            <li key={organization.id}>
              {organization.name} ({organization.slug})
            </li>
          ))}
        </ul>
      )}
    </main>
  )
  
}

export default App
