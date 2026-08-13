export type Organization = {
    id: string
    name: string
    slug: string
}

export type CreateOrganizationRequest ={
    name: string
    slug: string
}

export async function getOrganizations(): Promise<Organization[]> {
    const response = await fetch('/api/organizations')

    if (!response.ok) {
        throw new Error('Failed to load organizations')
    }

    return response.json()
}

export async function createOrganization(
    request: CreateOrganizationRequest,
): Promise<Organization> {
    const response = await fetch('/api/organizations', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(request),
    })

    if (!response.ok) {
        throw new Error('Failed to create organization')
    }

    return response.json()
}