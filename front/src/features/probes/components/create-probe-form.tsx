import {
    Field,
    FieldDescription,
    FieldGroup,
    FieldLabel,
    FieldLegend,
    FieldSet
} from "@/components/atoms/field.tsx";
import {Input} from "@/components/atoms/input.tsx";
import {Button} from "@/components/atoms/button.tsx";
import ProbeProtocol from "@/features/probes/enums/probe-enum.ts";
import {Bell} from "lucide-react";
import {Switch} from "@/components/atoms/switch.tsx";
import HttpStatusCode from "@/features/probes/enums/http-status-code.ts";
import {useStoreProbeForm} from "@/features/probes/hooks/useStoreProbeForm.ts";
import {Textarea} from "@/components/atoms/textarea.tsx";
import FormSwitch from "@/components/molecules/forms/form-switch.tsx";
import FormSelect from "@/components/molecules/forms/form-select.tsx";
import FormMultiSelect from "@/components/molecules/forms/form-select-multiple.tsx";

export default function CreateProbeForm() {
    const {form, isLoading, onsubmit, error} = useStoreProbeForm()
    const protocol = form.watch('protocol')

    if (protocol === ProbeProtocol.DNS) {
        form.setValue('url', "1.1.1.1")
    }

    return <div>
        <form onSubmit={form.handleSubmit(onsubmit)}>
            <div className="grid grid-cols-2 gap-4">
                <FieldGroup>
                    <FieldSet>
                        <FieldLegend>Create monitor</FieldLegend>
                        <FieldDescription>
                            All transactions are secure and encrypted
                        </FieldDescription>
                        <FieldGroup>
                            <Field className="space-y-2">
                                <FieldLabel htmlFor="checkout-7j9-card-name-43j">
                                    Monitor Protocol
                                </FieldLabel>
                                <FormSelect
                                    form={form}
                                    name="protocol"
                                    options={Object.values(ProbeProtocol)}
                                />
                            </Field>
                            <Field>
                                <FieldLabel htmlFor="name">
                                    Name on monitor
                                </FieldLabel>
                                <Input
                                    {...form.register("name")}
                                    id="name"
                                    placeholder="Evil Rabbit"
                                    required
                                />
                            </Field>
                            {protocol !== ProbeProtocol.DNS && (
                                <Field>
                                    <FieldLabel htmlFor="url">
                                        Monitor URL
                                    </FieldLabel>
                                    <Input
                                        {...form.register("url")}
                                        id="url"
                                        required
                                    />
                                </Field>
                            )}
                            {protocol === ProbeProtocol.TCP && (
                                <Field>
                                    <FieldLabel htmlFor="tcp_port">
                                        TCP Port
                                    </FieldLabel>
                                    <Input
                                        {...form.register("tcp_port", {valueAsNumber: true})}
                                        id="tcp_port"
                                        type="number"
                                        min={1}
                                        required
                                    />
                                </Field>
                            )}
                            {protocol === ProbeProtocol.DNS && (
                                <>
                                    <Field>
                                        <FieldLabel htmlFor="dns_server">
                                            DNS Server
                                        </FieldLabel>
                                        <Input
                                            {...form.register("url")}
                                            id="dns_server"
                                            defaultValue={"1.1.1.1"}
                                        />
                                        <FieldDescription>
                                            Cloudflare is the default server. You can change the resolver server
                                            anytime.
                                        </FieldDescription>
                                    </Field>
                                    <Field>
                                        <FieldLabel htmlFor="dns_port">
                                            DNS Port
                                        </FieldLabel>
                                        <Input
                                            {...form.register("dns_port")}
                                            id="dns_port"
                                            type="number"
                                            required
                                        />
                                        <FieldDescription>
                                            DNS server port. Defaults to 53. You can change the port at any time.
                                        </FieldDescription>
                                    </Field>
                                </>
                            )}
                            {protocol === ProbeProtocol.PING && (
                                <>
                                    <Field>
                                        <FieldLabel htmlFor="ping_heartbeat_interval">
                                            Heartbeat Interval
                                        </FieldLabel>
                                        <Input
                                            {...form.register("ping_heartbeat_interval")}
                                            id="ping_heartbeat_interval"
                                            required
                                        />
                                        <FieldDescription>
                                            1 min
                                        </FieldDescription>
                                    </Field>
                                </>
                            )}
                            <Field>
                                <FieldLabel htmlFor="retry">
                                    Retry
                                </FieldLabel>
                                <Input
                                    {...form.register("retry", {valueAsNumber: true})}
                                    id="retry"
                                    type="number"
                                    min={0}
                                    required
                                />
                            </Field>
                            <Field>
                                <FieldLabel htmlFor="interval">
                                    Interval value (in seconds) default 60
                                </FieldLabel>
                                <Input
                                    {...form.register("interval", {valueAsNumber: true})}
                                    id="interval"
                                    type="number"
                                    min={10}
                                    required
                                />
                            </Field>
                            <Field>
                                <FieldLabel htmlFor="interval_retry">
                                    Retry attempts
                                </FieldLabel>
                                <Input
                                    {...form.register("interval_retry", {valueAsNumber: true})}
                                    id="interval_retry"
                                    type="number"
                                    min={0}
                                    required
                                />
                                <FieldDescription>
                                    Maximum retries before the service is marked as down and a notification is sent
                                </FieldDescription>
                            </Field>
                        </FieldGroup>
                    </FieldSet>

                    <FieldGroup>
                        <FieldLegend>Advanced</FieldLegend>
                        <Field className="mb-2">
                            <div className="flex items-center space-x-2">
                                <FormSwitch form={form} name={'enabled'} label={"Enabled"}/>
                            </div>
                        </Field>
                        {protocol === ProbeProtocol.HTTP && (
                            <>
                                <Field className="mb-2">
                                    <div className="flex items-center space-x-2">
                                        <FormSwitch form={form} name={'notification_certificate'}
                                                    label={"Certificate Expiry Notification"}/>
                                    </div>
                                </Field>
                                <Field className="mb-2">
                                    <div className="flex items-center space-x-2">
                                        <FormSwitch form={form} name={'ignore_certificate_errors'}
                                                    label={"  Ignore TLS / SSL errors for HTTS websites"}/>
                                    </div>
                                </Field>


                                <FormMultiSelect
                                    form={form}
                                    label={"Accepted Status Codes"}
                                    name={"http_code_allowed"}
                                    options={Object.values(HttpStatusCode)}
                                    searchable={true}
                                    closeOnSelect={true}
                                />
                            </>
                        )}
                        {protocol === ProbeProtocol.PING && (
                            <>
                                <Field className="space-y-2">
                                    <FieldLabel htmlFor="ping_max_packet">
                                        Number of ping packets
                                    </FieldLabel>
                                    <Input
                                        {...form.register("ping_max_packet")}
                                        id="ping_max_packet"
                                        type="number"
                                        required
                                    />
                                </Field>

                                <Field className="mb-2">
                                    <div className="flex items-center space-x-2">
                                        <Switch id="airplane-mode"/>
                                        <FieldLabel htmlFor="airplane-mode">
                                            Numeric Output
                                        </FieldLabel>
                                    </div>
                                    <FieldDescription>
                                        If checked, IP addresses will be output instead of symbolic hostnames
                                    </FieldDescription>
                                </Field>


                                <Field className="space-y-2">
                                    <FieldLabel htmlFor="ping_size">
                                        Ping size package
                                    </FieldLabel>
                                    <Input
                                        {...form.register("ping_size")}
                                        id="ping_size"
                                        type="number"
                                        required
                                    />
                                </Field>


                                <Field className="space-y-2">
                                    <FieldLabel htmlFor="ping_delay">
                                        Ping delay (in seconds)
                                    </FieldLabel>
                                    <Input
                                        {...form.register("ping_delay")}
                                        id="ping_delay"
                                        type="number"
                                        required
                                    />
                                </Field>
                            </>
                        )}

                        <Field className="space-y-2">
                            <FieldLabel htmlFor="description">
                                Description
                            </FieldLabel>
                            <Textarea
                                {...form.register("description", {})}
                                id="description"
                                rows={5}
                            />
                        </Field>
                    </FieldGroup>
                </FieldGroup>


                <FieldGroup>
                    <FieldSet>
                        <FieldLegend className="flex items-center gap-2">
                            <Bell/> Notifications</FieldLegend>
                        <FieldGroup>

                            <Button>Create new notification</Button>
                        </FieldGroup>
                    </FieldSet>
                </FieldGroup>
            </div>

            <FieldGroup className="mt-6">
                <Field orientation="horizontal">
                    <Button type="submit">{isLoading ? "...Creating" : 'Create'}</Button>
                    <Button variant="outline" type="button">
                        Cancel
                    </Button>
                </Field>
            </FieldGroup>
        </form>
    </div>
}